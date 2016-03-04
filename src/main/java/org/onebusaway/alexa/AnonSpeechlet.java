/*
 * Copyright 2016 Philip M. White (philip@mailworks.org)
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
import com.amazon.speech.ui.Reprompt;
import lombok.extern.log4j.Log4j;
import org.onebusaway.alexa.lib.GoogleMaps;
import org.onebusaway.alexa.lib.ObaClient;
import org.onebusaway.alexa.lib.ObaUserClient;
import org.onebusaway.alexa.storage.ObaDao;
import org.onebusaway.alexa.storage.ObaUserDataItem;
import org.onebusaway.io.client.elements.ObaRegion;
import org.onebusaway.io.client.elements.ObaStop;
import org.onebusaway.location.Location;

import javax.annotation.Resource;
import java.net.URISyntaxException;
import java.util.Optional;

import static org.onebusaway.alexa.SessionAttributes.*;

@Log4j
public class AnonSpeechlet implements Speechlet {
    @Resource
    private ObaDao obaDao;

    @Resource
    private GoogleMaps googleMaps;

    @Resource
    private ObaClient obaClient;

    private enum OnboardState {Fresh, OnlyCity}

    private final static Reprompt cityReprompt;
    private final static Reprompt stopNumReprompt;

    static {
        PlainTextOutputSpeech citySpeech = new PlainTextOutputSpeech();
        citySpeech.setText("What is your city?");
        cityReprompt = new Reprompt();
        cityReprompt.setOutputSpeech(citySpeech);

        PlainTextOutputSpeech stopNumSpeech = new PlainTextOutputSpeech();
        stopNumSpeech.setText("What is your stop number?  You can find your stop's number on the placard in the bus zone, or in your OneBusAway app.");
        stopNumReprompt = new Reprompt();
        stopNumReprompt.setOutputSpeech(stopNumSpeech);
    }

    @Override
    public void onSessionStarted(SessionStartedRequest sessionStartedRequest, Session session) throws SpeechletException {

    }

    @Override
    public SpeechletResponse onLaunch(LaunchRequest launchRequest, Session session) throws SpeechletException {
        switch (getOnboardState(session)) {
            case Fresh:
                return askForCity(Optional.empty());
            case OnlyCity:
                return reaskForStopNumber();
            default:
                throw new SpeechletException("Unrecognized onboard state");
        }
    }

    @Override
    public SpeechletResponse onIntent(IntentRequest request, Session session)
            throws SpeechletException {
        Intent intent = request.getIntent();
        if ("SetCityIntent".equals(intent.getName())) {
            String cityName = intent.getSlot(CITY_NAME.toString()).getValue();
            Optional<Location> location = googleMaps.geocode(cityName);
            if (!location.isPresent()) {
                // Couldn't find the city at all.
                return askForCity(Optional.of(cityName));
            }

            // Get closest region from geographic location
            Optional<ObaRegion> region = ObaUserClient.getClosestRegion(location.get());

            if (!region.isPresent() || region.get().getObaBaseUrl() == null) {
                // Couldn't find a nearby region that supports the OBA REST API
                return askForCity(Optional.of(cityName));
            } else {
                // Got a region!
                session.setAttribute(CITY_NAME.toString(), cityName);
                session.setAttribute(REGION_ID.toString(), region.get().getId());
                session.setAttribute(REGION_NAME.toString(), region.get().getName());
                session.setAttribute(OBA_BASE_URL.toString(), region.get().getObaBaseUrl());
                PlainTextOutputSpeech out = new PlainTextOutputSpeech();
                out.setText(String.format("Ok, we found the %s region near you.  What's your stop number?",
                        region.get().getName()));
                return SpeechletResponse.newAskResponse(out, stopNumReprompt);
            }
        } else if ("GetCityIntent".equals(intent.getName())) {
            String city = (String)session.getAttribute(CITY_NAME.toString());
            if (city == null) {
                PlainTextOutputSpeech out = new PlainTextOutputSpeech();
                out.setText("You have not yet told me where you live.  What is your city?");
                return SpeechletResponse.newAskResponse(out, cityReprompt);
            } else {
                PlainTextOutputSpeech out = new PlainTextOutputSpeech();
                out.setText(
                        String.format("You just told me you live in %s, " +
                                        "but we still need your stop number. " +
                                        "What's your stop number?",
                                city));
                return SpeechletResponse.newAskResponse(out, stopNumReprompt);
            }
        } else if ("SetStopNumberIntent".equals(intent.getName())) {
            String stopNumberStr = intent.getSlot("stopNumber").getValue();
            log.debug("Stop number string received: " + stopNumberStr);
            return setStopNumber(
                    Integer.valueOf(stopNumberStr),
                    session);
        } else {
            throw new SpeechletException("Did not recognize intent name");
        }
    }

    @Override
    public void onSessionEnded(SessionEndedRequest sessionEndedRequest, Session session) throws SpeechletException {

    }

    private OnboardState getOnboardState(Session session) {
        if (session.getAttribute(CITY_NAME.toString()) != null) {
            return OnboardState.OnlyCity;
        } else
            return OnboardState.Fresh;
    }

    private SpeechletResponse reaskForStopNumber() {
        PlainTextOutputSpeech out = new PlainTextOutputSpeech();
        out.setText("OneBusAway could not locate your stop number.  Tell me again, what is your stop number?");
        return SpeechletResponse.newAskResponse(out, stopNumReprompt);
    }

    private SpeechletResponse setStopNumber(int spokenStopNumber, Session session) {
        String cityName = (String) session.getAttribute(CITY_NAME.toString());
        String regionName = (String) session.getAttribute(REGION_NAME.toString());
        log.debug(String.format(
                "Asked to set stop number %d in city %s for region %s...", spokenStopNumber, cityName, regionName));
        if (cityName == null) {
            return askForCity(Optional.empty());
        }

        // Map city name to a geographic location
        Optional<Location> location = googleMaps.geocode(cityName);
        if (!location.isPresent()) {
            return askForCity(Optional.of(cityName));
        }

        // Get closest region from geographic location
        Optional<ObaRegion> region = ObaUserClient.getClosestRegion(location.get());
        if (!region.isPresent() || region.get().getObaBaseUrl() == null) {
            // Couldn't find a nearby region that supports the OBA REST API
            return askForCity(Optional.of(cityName));
        }


        ObaUserClient obaUserClient;
        try {
            obaUserClient = obaClient.withObaBaseUrl(region.get().getObaBaseUrl());
        } catch (URISyntaxException e) {
            log.error("ObaBaseUrl " + region.get().getObaBaseUrl() + " for " + region.get().getName()
                    + " is invalid: " + e.getMessage());
            // Region didn't have a valid URL - ask again and hopefully we find a different one
            return askForCity(Optional.of(cityName));
        }

        ObaStop[] searchResults = obaUserClient.getStopFromCode(location.get(), spokenStopNumber);
        if (searchResults.length == 0) {
            return reaskForStopNumber();
        } else if (searchResults.length > 1) {
            PlainTextOutputSpeech out = new PlainTextOutputSpeech();
            out.setText(String.format("I expected to find exactly one stop for the number " +
                            "you gave, but instead found %d.  This is not yet supported.",
                    searchResults.length));
            return SpeechletResponse.newTellResponse(out);
        } else {
            // Perfect!
            return createOrUpdateUser(session, cityName, searchResults[0], region.get());
        }
    }

    private SpeechletResponse createOrUpdateUser(Session session,
                                                 String cityName,
                                                 ObaStop stop,
                                                 ObaRegion region) {
        log.debug(String.format(
                "Crupdating user with city %s and stop ID %s, code %s, regionId %d, regionName %s, obaBaseUrl %s.",
                cityName, stop.getId(), stop.getStopCode(), region.getId(), region.getName(), region.getObaBaseUrl()));
        Optional<ObaUserDataItem> optUserData = obaDao.getUserData(session);
        if (optUserData.isPresent()) {
            ObaUserDataItem userData = optUserData.get();
            userData.setCity(cityName);
            userData.setStopId(stop.getId());
            userData.setRegionId(region.getId());
            userData.setRegionName(region.getName());
            userData.setObaBaseUrl(region.getObaBaseUrl());
            obaDao.saveUserData(userData);
        }
        else {
            ObaUserDataItem userData = new ObaUserDataItem(
                    session.getUser().getUserId(),
                    cityName,
                    stop.getId(),
                    region.getId(),
                    region.getName(),
                    region.getObaBaseUrl(),
                    null
            );
            obaDao.saveUserData(userData);
        }

        PlainTextOutputSpeech out = new PlainTextOutputSpeech();
        out.setText(String.format("Ok, your stop number is %s in the %s region. " +
                        "Great.  I am ready to tell you about the next bus.",
                stop.getStopCode(), region.getName()));
        return SpeechletResponse.newTellResponse(out);
    }

    private SpeechletResponse askForCity(Optional<String> currentCityName) {
        PlainTextOutputSpeech out = new PlainTextOutputSpeech();
        if (currentCityName.isPresent()) {
            out.setText(String.format("OneBusAway could not locate a OneBusAway " +
                            "region near %s, the city you gave. " +
                            "Tell me again, in what city do you live?",
                    currentCityName.get()));
        } else {
            out.setText("Welcome to OneBusAway! Let's set you up. " +
                    "You'll need your city and your stop number. " +
                    "The stop number is shown on the placard in the bus zone, " +
                    "on your transit agency's web site, " +
                    "or in your OneBusAway mobile app. " +
                    "In what city do you live?");
        }
        return SpeechletResponse.newAskResponse(out, cityReprompt);
    }
}
