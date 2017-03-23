/*
 * Copyright 2016-2017 Sean J. Barbeau (sjbarbeau@gmail.com),
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
import org.onebusaway.alexa.util.RouteFilterUtil;
import org.onebusaway.alexa.util.SessionUtil;
import org.onebusaway.alexa.util.SpeechUtil;
import org.onebusaway.alexa.util.StorageUtil;
import org.onebusaway.io.client.elements.ObaRoute;
import org.onebusaway.io.client.request.ObaArrivalInfoResponse;
import org.onebusaway.io.client.request.ObaStopResponse;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import static org.onebusaway.alexa.ObaIntent.*;
import static org.onebusaway.alexa.SessionAttribute.*;
import static org.onebusaway.alexa.lib.ObaUserClient.ARRIVALS_SCAN_MINS;

@NoArgsConstructor
@Log4j
public class AuthedSpeechlet implements Speechlet {

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
        SessionUtil.populateAttributes(session, Optional.of(userData));
        AskState askState = SessionUtil.getAskState(session);
        session.setAttribute(ASK_STATE, AskState.NONE.toString());

        Intent intent = request.getIntent();
        if (HELP.equals(intent.getName())) {
            return SpeechUtil.getHelpMessage();
        } else if (REPEAT.equals(intent.getName())) {
            PlainTextOutputSpeech out = new PlainTextOutputSpeech();
            out.setText(StorageUtil.getCachedResponse(session, userData));
            return SpeechletResponse.newTellResponse(out);
        } else if (SET_CITY.equals(intent.getName())) {
            return anonSpeechlet.onIntent(request, session);
        } else if (GET_CITY.equals(intent.getName())) {
            return getCity();
        } else if (SET_STOP_NUMBER.equals(intent.getName())) {
            return anonSpeechlet.onIntent(request, session);
        } else if (YES.equals(intent.getName())) {
            return handleYesIntent(request, session, askState);
        } else if (NO.equals(intent.getName())) {
            return handleNoIntent(request, session, askState);
        } else if (GET_STOP_NUMBER.equals(intent.getName())) {
            return getStopDetails();
        } else if (GET_ARRIVALS.equals(intent.getName())) {
            return tellArrivals(session);
        } else if (ENABLE_CLOCK_TIME.equals(intent.getName())) {
            return enableClockTime(session);
        } else if (DISABLE_CLOCK_TIME.equals(intent.getName())) {
            return disableClockTime(session);
        } else if (SET_ROUTE_FILTER.equals(intent.getName())) {
            return setRouteFilter(session);
        } else if (ENABLE_EXPERIMENTAL_REGIONS.equals(intent.getName())) {
            return enableExperimentalRegions(session);
        } else if (DISABLE_EXPERIMENTAL_REGIONS.equals(intent.getName())) {
            return disableExperimentalRegions(session);
        } else if (STOP.equals(intent.getName()) || CANCEL.equals(intent.getName())) {
            return SpeechUtil.goodbye();
        } else {
            throw new SpeechletException("Did not recognize intent name");
        }
    }

    @Override
    public SpeechletResponse onLaunch(final LaunchRequest request,
                                      final Session session)
            throws SpeechletException {
        SessionUtil.populateAttributes(session, Optional.of(userData));
        return tellArrivals(session);
    }

    @Override
    public void onSessionStarted(final SessionStartedRequest request,
                                 final Session session) {
    }

    @Override
    public void onSessionEnded(final SessionEndedRequest request,
                               final Session session) {
    }

    private SpeechletResponse getCity() {
        String output = String.format("You live in %s, near the %s region.", userData.getCity(), userData.getRegionName());
        StorageUtil.saveOutputForRepeat(output, obaDao, userData);
        PlainTextOutputSpeech out = new PlainTextOutputSpeech();
        out.setText(output);
        return SpeechletResponse.newTellResponse(out);
    }

    private SpeechletResponse getStopDetails() throws SpeechletException {
        ObaStopResponse stop;
        try {
            stop = obaUserClient.getStopDetails(userData.getStopId());
        } catch (IOException e) {
            throw new SpeechletException(e);
        }
        String output = String.format("Your stop is %s, %s.", stop.getStopCode(), stop.getName());
        StorageUtil.saveOutputForRepeat(output, obaDao, userData);
        PlainTextOutputSpeech out = new PlainTextOutputSpeech();
        out.setText(output);
        return SpeechletResponse.newTellResponse(out);
    }

    private SpeechletResponse tellArrivals(Session session) throws SpeechletException {
        ObaArrivalInfoResponse response;
        try {
            response = obaUserClient.getArrivalsAndDeparturesForStop(
                    userData.getStopId(),
                    ARRIVALS_SCAN_MINS
            );
        } catch (IOException e) {
            throw new SpeechletException(e);
        }

        String timeZoneText = userData.getTimeZone();
        log.debug("time zone is " + timeZoneText);
        TimeZone timeZone = null;
        if (!TextUtils.isEmpty(timeZoneText)) {
            timeZone = TimeZone.getTimeZone(timeZoneText);
        }

        HashSet routesToFilter = SpeechUtil.getRoutesToFilter(obaDao, session);

        String output = SpeechUtil.getArrivalText(response.getArrivalInfo(), ARRIVALS_SCAN_MINS,
                response.getCurrentTime(), userData.getSpeakClockTime(), timeZone, routesToFilter);

        log.info("Full text output: " + output);

        // Build the full text response to the user
        StringBuilder builder = new StringBuilder();
        builder.append(SpeechUtil.getAnnounceFeaturev1_1_0Text(session));
        builder.append(output);

        // Save that we've already read the tutorial info to the user
        userData.setAnnouncedIntroduction(1L);
        userData.setAnnouncedFeaturesv1_1_0(1L);
        obaDao.saveUserData(userData);

        StorageUtil.saveOutputForRepeat(builder.toString(), obaDao, userData);
        PlainTextOutputSpeech out = new PlainTextOutputSpeech();
        out.setText(builder.toString());
        return SpeechletResponse.newTellResponse(out);
    }

    private SpeechletResponse enableClockTime(Session session) throws SpeechletException {
        return StorageUtil.updateClockTime(1, session, obaDao, userData, obaUserClient);

    }

    private SpeechletResponse disableClockTime(Session session) throws SpeechletException {
        return StorageUtil.updateClockTime(0, session, obaDao, userData, obaUserClient);
    }

    /**
     * User asked to filter routes for the currently selected stop, so return the initial response
     *
     * @param session
     * @return the initial response for setting a route filter
     */
    private SpeechletResponse setRouteFilter(final Session session) {
        // Make sure we clear any existing routes to filter for this session (but leave any persisted)
        session.setAttribute(DIALOG_ROUTES_TO_FILTER, null);

        String stopId = (String) session.getAttribute(STOP_ID);
        String regionName = (String) session.getAttribute(REGION_NAME);
        log.debug(String.format(
                "Asked to set a route filter for stop ID %s in region %s...", stopId, regionName));

        ObaStopResponse response;
        try {
            response = obaUserClient.getStop(stopId);
        } catch (IOException e) {
            log.error("Error getting details for stop + " + stopId + " in region " + regionName + ": " + e.getMessage());
            return SpeechUtil.getCommunicationErrorMessage();
        }

        List<ObaRoute> routes = response.getRoutes();
        if (routes.size() <= 1) {
            String output = String.format("There is only one route for stop " + response.getStopCode() + ", so I can't filter out any routes.");
            StorageUtil.saveOutputForRepeat(output, obaDao, userData);
            PlainTextOutputSpeech out = new PlainTextOutputSpeech();
            out.setText(output);
            return SpeechletResponse.newTellResponse(out);
        }
        session.setAttribute(STOP_CODE, response.getStopCode());

        // There is more than one route - ask user which they want to hear arrivals for
        return RouteFilterUtil.askUserAboutFilterRoute(session, routes);
    }

    private SpeechletResponse handleYesIntent(final IntentRequest request,
                                              final Session session, AskState askState) throws SpeechletException {
        if (askState == AskState.VERIFYSTOP) {
            // User confirmed that they want to select a particular stop - pass to anonSpeechlet to finish dialog
            // Restore the session ASK_STATE
            session.setAttribute(ASK_STATE, askState.toString());
            return anonSpeechlet.onIntent(request, session);
        }

        if (askState == AskState.FILTER_INDIVIDUAL_ROUTE) {
            return RouteFilterUtil.handleFilterRouteResponse(session, true, obaDao, userData);
        }

        log.error("Received yes intent without a question.");
        return SpeechUtil.getGeneralErrorMessage();
    }

    private SpeechletResponse handleNoIntent(final IntentRequest request,
                                             Session session, AskState askState) throws SpeechletException {
        if (askState == AskState.VERIFYSTOP) {
            // User said no to the current stop, so ask about the next one - pass to anonSpeechlet to finish dialog
            // Restore the session ASK_STATE
            session.setAttribute(ASK_STATE, askState.toString());
            return anonSpeechlet.onIntent(request, session);
        }

        if (askState == AskState.FILTER_INDIVIDUAL_ROUTE) {
            return RouteFilterUtil.handleFilterRouteResponse(session, false, obaDao, userData);
        }

        log.error("Received no intent without a question.");
        return SpeechUtil.getGeneralErrorMessage();
    }

    private SpeechletResponse enableExperimentalRegions(Session session) throws SpeechletException {
        return StorageUtil.updateExperimentalRegions(true, session, obaDao, userData, obaUserClient);

    }

    private SpeechletResponse disableExperimentalRegions(Session session) throws SpeechletException {
        return StorageUtil.updateExperimentalRegions(false, session, obaDao, userData, obaUserClient);
    }
}
