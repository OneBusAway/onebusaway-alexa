/*
 * Copyright 2016 Sean J. Barbeau (sjbarbeau@gmail.com),
 * Philip M. White (philip@mailworks.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.alexa;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.*;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.log4j.Log4j;
import org.apache.http.util.TextUtils;
import org.onebusaway.alexa.lib.ObaUserClient;
import org.onebusaway.alexa.storage.ObaDao;
import org.onebusaway.alexa.storage.ObaUserDataItem;
import org.onebusaway.io.client.elements.ObaArrivalInfo;
import org.onebusaway.io.client.request.ObaArrivalInfoResponse;
import org.onebusaway.io.client.request.ObaStopResponse;
import org.onebusaway.io.client.util.ArrivalInfo;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.onebusaway.alexa.ObaIntent.*;
import static org.onebusaway.alexa.SessionAttribute.*;

@NoArgsConstructor
@Log4j
public class AuthedSpeechlet implements Speechlet {
    public static final int ARRIVALS_SCAN_MINS = 35;

    @Resource
    private ObaDao obaDao;

    @Resource
    private AnonSpeechlet anonSpeechlet;

    private ObaUserClient obaUserClient;

    private ObaUserDataItem userData;

    public void setUserData(@NonNull ObaUserDataItem userData) throws URISyntaxException {
        this.userData = userData;
        this.obaUserClient = new ObaUserClient(userData.getObaBaseUrl());
    }

    @Override
    public SpeechletResponse onIntent(final IntentRequest request,
                                      final Session session)
            throws SpeechletException {
        populateAttributes(session);

        Intent intent = request.getIntent();
        if (HELP.equals(intent.getName())) {
            PlainTextOutputSpeech out = new PlainTextOutputSpeech();
            out.setText("The OneBusAway skill will tell you upcoming transit arrivals " +
            "at a stop of your choice.  You've already configured your region and stop, " +
            "so just open the skill or ask me for arrivals. " +
            "You can also ask me to change your city or stop.");
            return SpeechletResponse.newTellResponse(out);
        } else if (REPEAT.equals(intent.getName())) {
            PlainTextOutputSpeech out = new PlainTextOutputSpeech();
            out.setText(getCachedResponse(session));
            return SpeechletResponse.newTellResponse(out);
        } else if (SET_CITY.equals(intent.getName())) {
            return anonSpeechlet.onIntent(request, session);
        } else if (GET_CITY.equals(intent.getName())) {
            return getCity();
        } else if (SET_STOP_NUMBER.equals(intent.getName())) {
            return anonSpeechlet.onIntent(request, session);
        } else if (GET_STOP_NUMBER.equals(intent.getName())) {
            return getStopDetails();
        } else if (GET_ARRIVALS.equals(intent.getName())) {
            return tellArrivals();
        } else if (STOP.equals(intent.getName())) {
            return goodbye();
        } else {
            throw new SpeechletException("Did not recognize intent name");
        }
    }

    @Override
    public SpeechletResponse onLaunch(final LaunchRequest request,
                                      final Session session)
            throws SpeechletException {
        populateAttributes(session);
        return tellArrivals();
    }

    @Override
    public void onSessionStarted(final SessionStartedRequest request,
                                 final Session session) {

    }

    @Override
    public void onSessionEnded(final SessionEndedRequest request,
                               final Session session) {

    }

    /**
     * Populates the provided session with persisted user data, if the session attribute is empty
     * @param session
     */
    private void populateAttributes(Session session) {
        if (session.getAttribute(CITY_NAME) == null) {
            session.setAttribute(CITY_NAME, userData.getCity());
        }
        if (session.getAttribute(STOP_NUMBER) == null) {
            session.setAttribute(STOP_NUMBER, userData.getStopId());
        }
        if (session.getAttribute(REGION_ID) == null) {
            session.setAttribute(REGION_ID, userData.getRegionId());
        }
        if (session.getAttribute(REGION_NAME) == null) {
            session.setAttribute(REGION_NAME, userData.getRegionName());
        }
        if (session.getAttribute(OBA_BASE_URL) == null) {
            session.setAttribute(OBA_BASE_URL, userData.getObaBaseUrl());
        }
        if (session.getAttribute(PREVIOUS_RESPONSE) == null) {
            session.setAttribute(PREVIOUS_RESPONSE, userData.getPreviousResponse());
        }
    }

    private SpeechletResponse getCity() {
        String output = String.format("You live in %s, near the %s region.", userData.getCity(), userData.getRegionName());
        saveOutputForRepeat(output);
        PlainTextOutputSpeech out = new PlainTextOutputSpeech();
        out.setText(output);
        return SpeechletResponse.newTellResponse(out);
    }

    private SpeechletResponse getStopDetails() throws SpeechletException {
        ObaStopResponse stop = null;
        try {
            stop = obaUserClient.getStopDetails(userData.getStopId());
        } catch (IOException e) {
            throw new SpeechletException(e);
        }
        String output = String.format("Your stop is %s, %s.", stop.getStopCode(), stop.getName());
        saveOutputForRepeat(output);
        PlainTextOutputSpeech out = new PlainTextOutputSpeech();
        out.setText(output);
        return SpeechletResponse.newTellResponse(out);
    }

    private SpeechletResponse tellArrivals() throws SpeechletException {
        ObaArrivalInfoResponse response = null;
        try {
            response = obaUserClient.getArrivalsAndDeparturesForStop(
                    userData.getStopId(),
                    ARRIVALS_SCAN_MINS
            );
        } catch (IOException e) {
            throw new SpeechletException(e);
        }
        ObaArrivalInfo[] arrivals = response.getArrivalInfo();

        String output;

        if (arrivals.length == 0) {
            output = "There are no upcoming arrivals at your stop for the next "
                    + ARRIVALS_SCAN_MINS + " minutes.";
        } else {
            StringBuilder sb = new StringBuilder();
            for (ObaArrivalInfo obaArrival: arrivals) {
                log.info("Arrival: " + obaArrival);
                ArrivalInfo arrival = new ArrivalInfo(obaArrival, response.getCurrentTime());
                sb.append(arrival.getLongDescription() + " -- "); //with pause between sentences
            }
            output = sb.toString();
        }
        log.info("Full text output: " + output);
        saveOutputForRepeat(output);
        PlainTextOutputSpeech out = new PlainTextOutputSpeech();
        out.setText(output);
        return SpeechletResponse.newTellResponse(out);
    }

    private void saveOutputForRepeat(String output) {
        log.debug("Caching output for repeat = " + output);
        userData.setPreviousResponse(output);
        obaDao.saveUserData(userData);
    }

    /**
     * Gets a previously cached response to repeat to the user
     * @param session
     * @return a previously cached response to repeat to the user, or a friendly error message
     * if there isn't anything to repeat.
     */
    private String getCachedResponse(Session session) {
        // Try session first
        String lastOutput = (String) session.getAttribute(PREVIOUS_RESPONSE);
        if (!TextUtils.isEmpty(lastOutput)) {
            log.debug("Repeating last output from session = " + lastOutput);
            return lastOutput;
        }
        // Try persisted data
        lastOutput = userData.getPreviousResponse();
        if (!TextUtils.isEmpty(lastOutput)) {
            log.debug("Repeating last output from obaDao = " + lastOutput);
            return lastOutput;
        }
        return "I'm sorry, I don't have anything to repeat.  You can ask me for arrival times for your stop.";
    }

    private SpeechletResponse goodbye() {
        String output = String.format("Good-bye");
        PlainTextOutputSpeech out = new PlainTextOutputSpeech();
        out.setText(output);
        return SpeechletResponse.newTellResponse(out);
    }
}
