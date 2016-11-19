/*
 * Copyright 2016 Philip M. White (philip@mailworks.org),
 * Sean J. Barbeau (sjbarbeau@gmail.com)
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
import org.onebusaway.alexa.util.SpeechUtil;
import org.onebusaway.io.client.elements.ObaRegion;
import org.onebusaway.io.client.elements.ObaStop;
import org.onebusaway.io.client.request.ObaArrivalInfoResponse;
import org.onebusaway.io.client.util.RegionUtils;
import org.onebusaway.location.Location;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.onebusaway.alexa.ObaIntent.*;
import static org.onebusaway.alexa.SessionAttribute.*;
import static org.onebusaway.alexa.lib.ObaUserClient.ARRIVALS_SCAN_MINS;

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
        AskState askState = getAskState(session);
        session.setAttribute(ASK_STATE, AskState.NONE.toString());
        if (HELP.equals(intent.getName()) ||
                GET_ARRIVALS.equals(intent.getName()) ||
                GET_STOP_NUMBER.equals(intent.getName()) ||
                REPEAT.equals(intent.getName())) {
            // User asked for help, or we don't yet have enough information to respond.  Return welcome message.
            return askForCity(Optional.empty());
        } else if (SET_CITY.equals(intent.getName())) {
            String cityName = intent.getSlot(CITY_NAME).getValue();
            if (cityName == null) {
                return askForCity(Optional.empty());
            }

            Optional<Location> location = googleMaps.geocode(cityName);
            if (!location.isPresent()) {
                // Couldn't find the city at all.
                return askForCity(Optional.of(cityName));
            }

            // Get closest region from geographic location
            Optional<ObaRegion> region;
            try {
                region = obaClient.getClosestRegion(location.get());
            } catch (IOException e) {
                log.error("Error getting closest region: " + e.getMessage());
                return askForCity(Optional.of(cityName));
            }

            if (!region.isPresent() || region.get().getObaBaseUrl() == null) {
                // Couldn't find a nearby region that supports the OBA REST API
                return askForCity(Optional.of(cityName));
            } else {
                // Got a region!
                session.setAttribute(CITY_NAME, cityName);
                session.setAttribute(REGION_ID, region.get().getId());
                session.setAttribute(REGION_NAME, region.get().getName());
                session.setAttribute(OBA_BASE_URL, region.get().getObaBaseUrl());
                PlainTextOutputSpeech out = new PlainTextOutputSpeech();
                out.setText(String.format("Ok, we found the %s region near you.  What's your stop number?",
                        region.get().getName()));
                return SpeechletResponse.newAskResponse(out, stopNumReprompt);
            }
        } else if (GET_CITY.equals(intent.getName())) {
            String city = (String)session.getAttribute(CITY_NAME);
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
        } else if (SET_STOP_NUMBER.equals(intent.getName())) {
            String stopNumberStr = intent.getSlot(STOP_NUMBER).getValue();
            if (stopNumberStr == null) {
                PlainTextOutputSpeech out = new PlainTextOutputSpeech();
                out.setText("What is your stop number?");
                return SpeechletResponse.newAskResponse(out, stopNumReprompt);
            }
            log.debug("Stop number string received: " + stopNumberStr);
            return setStopNumber(
                    stopNumberStr,
                    session);
        } else if (YES.equals(intent.getName())) {
            return handleYesIntent(session, askState);
        } else if (NO.equals(intent.getName())) {
            return handleNoIntent(session, askState);
        } else if (STOP.equals(intent.getName()) || CANCEL.equals(intent.getName())) {
            return goodbye();
        } else {
            throw new SpeechletException("Did not recognize intent name");
        }
    }

    @Override
    public void onSessionEnded(SessionEndedRequest sessionEndedRequest, Session session) throws SpeechletException {

    }

    private SpeechletResponse handleYesIntent(Session session, AskState askState) throws SpeechletException {
        if (askState == AskState.VERIFYSTOP) {
            return handleVerifyStopResponse(session, true /*stopFound*/);
        }

        log.error("Received yes intent without a question.");
        return askForCity(Optional.empty());
    }

    private SpeechletResponse handleNoIntent(Session session, AskState askState) throws SpeechletException {
        if (askState == AskState.VERIFYSTOP) {
            return handleVerifyStopResponse(session, false /*stopFound*/);
        }

        log.error("Received no intent without a question.");
        return askForCity(Optional.empty());
    }

    private AskState getAskState(Session session) {
        AskState askState = AskState.NONE;
        String savedAskState = (String)session.getAttribute(ASK_STATE);
        if (savedAskState != null) {
            askState = AskState.valueOf(savedAskState);
        }
        return askState;
    }

    private SpeechletResponse handleVerifyStopResponse(Session session, boolean stopFound) throws SpeechletException {
        ArrayList<ObaStop> stops = (ArrayList<ObaStop>) session.getAttribute(FOUND_STOPS);
        if (stops != null) {
            if (stopFound && stops.size() > 0) {
                String cityName = (String)session.getAttribute(CITY_NAME);

                Optional<Location> location = googleMaps.geocode(cityName);
                if (!location.isPresent()) {
                    return askForCity(Optional.of(cityName));
                }

                Optional<ObaRegion> region = null;
                try {
                    region = obaClient.getClosestRegion(location.get());
                } catch (IOException e) {
                    log.error("Error getting closest region: " + e.getMessage());
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

                LinkedHashMap<String, String> stopData = (LinkedHashMap<String, String>) stops.get(0);
                return createOrUpdateUser(session, cityName, stopData.get("id"), stopData.get("stopCode"), region.get(), obaUserClient);
            } else if (!stopFound && stops.size() > 1) {
                stops.remove(0);
                session.setAttribute(FOUND_STOPS, stops);
                return askToVerifyStop(session, null);
            }
        }

        return reaskForStopNumber();
    }

    private OnboardState getOnboardState(Session session) {
        if (session.getAttribute(CITY_NAME) != null) {
            return OnboardState.OnlyCity;
        } else
            return OnboardState.Fresh;
    }

    private SpeechletResponse reaskForStopNumber() {
        PlainTextOutputSpeech out = new PlainTextOutputSpeech();
        out.setText("OneBusAway could not locate your stop number.  Tell me again, what is your stop number?");
        return SpeechletResponse.newAskResponse(out, stopNumReprompt);
    }

    private SpeechletResponse setStopNumber(String spokenStopNumber, Session session) throws SpeechletException {
        String cityName = (String) session.getAttribute(CITY_NAME);
        String regionName = (String) session.getAttribute(REGION_NAME);
        log.debug(String.format(
                "Asked to set stop number %s in city %s for region %s...", spokenStopNumber, cityName, regionName));
        if (cityName == null) {
            return askForCity(Optional.empty());
        }

        // Map city name to a geographic location - even if we've done this before, we want to refresh the info
        Optional<Location> location = googleMaps.geocode(cityName);
        if (!location.isPresent()) {
            return askForCity(Optional.of(cityName));
        }

        // Get closest region from geographic location
        Optional<ObaRegion> region = null;
        try {
            region = obaClient.getClosestRegion(location.get());
        } catch (IOException e) {
            log.error("Error getting closest region: " + e.getMessage());
            return askForCity(Optional.of(cityName));
        }
        if (!region.isPresent() || !RegionUtils.isRegionUsable(region.get())) {
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

        ObaStop[] searchResults;
        try {
            searchResults = obaUserClient.getStopFromCode(location.get(), spokenStopNumber);
        } catch (IOException e) {
            log.error("Couldn't get stop from code " + spokenStopNumber + ": " + e.getMessage());
            return reaskForStopNumber();
        }

        if (searchResults.length == 0) {
            return reaskForStopNumber();
        } else if (searchResults.length > 1) {
            return askToVerifyStop(session, searchResults);
        } else {
            // Perfect!
            return createOrUpdateUser(session, cityName, searchResults[0], region.get(), obaUserClient);
        }
    }

    private SpeechletResponse askToVerifyStop(Session session, ObaStop[] stops) {
        PlainTextOutputSpeech askForVerifyStop = new PlainTextOutputSpeech();
        String stopName = "";

        if (stops != null && stops.length > 0) {
            session.setAttribute(FOUND_STOPS, stops);
            stopName = stops[0].getName();
            askForVerifyStop.setText(String.format("We found %d stops associated with the stop number. Did you mean the %s stop?", stops.length, stopName));
        } else {
            ArrayList<ObaStop> foundStops = (ArrayList<ObaStop>) session.getAttribute(FOUND_STOPS);
            LinkedHashMap<String, String> stopData = (LinkedHashMap<String, String>) foundStops.get(0);
            stopName = stopData.get("name");
            askForVerifyStop.setText(String.format("Ok, what about the %s stop?", stopName));
        }

        PlainTextOutputSpeech verifyStopSpeech = new PlainTextOutputSpeech();
        verifyStopSpeech.setText(String.format("Did you mean the %s stop?", stopName));
        Reprompt verifyStopReprompt = new Reprompt();
        verifyStopReprompt.setOutputSpeech(verifyStopSpeech);

        session.setAttribute(ASK_STATE, AskState.VERIFYSTOP.toString());
        return SpeechletResponse.newAskResponse(askForVerifyStop, verifyStopReprompt);
    }

    private SpeechletResponse createOrUpdateUser(Session session,
                                                 String cityName,
                                                 ObaStop stop,
                                                 ObaRegion region, 
                                                 ObaUserClient obaUserClient) throws SpeechletException {
        return createOrUpdateUser(session, cityName, stop.getId(), stop.getStopCode(), region, obaUserClient);
    }

    private SpeechletResponse createOrUpdateUser(Session session, String cityName, String stopId, String stopCode, ObaRegion region, ObaUserClient obaUserClient) throws SpeechletException {
        log.debug(String.format(
            "Crupdating user with city %s and stop ID %s, code %s, regionId %d, regionName %s, obaBaseUrl %s.",
            cityName, stopId, stopCode, region.getId(), region.getName(), region.getObaBaseUrl()));

        ObaArrivalInfoResponse response;
        try {
            response = obaUserClient.getArrivalsAndDeparturesForStop(
                    stopId,
                    ARRIVALS_SCAN_MINS
            );
        } catch (IOException e) {
            throw new SpeechletException(e);
        }

        String arrivalInfoText = SpeechUtil.getArrivalText(response.getArrivalInfo(), ARRIVALS_SCAN_MINS, response.getCurrentTime());

        log.info("Full arrival text output: " + arrivalInfoText);
        String outText = String.format("Ok, your stop number is %s in the %s region. " +
                        "Great.  I am ready to tell you about the next bus.  You can always ask me for arrival times " +
                        "by saying 'open One Bus Away'.  Right now, %s",
                stopCode, region.getName(), arrivalInfoText);

        Optional<ObaUserDataItem> optUserData = obaDao.getUserData(session);
        if (optUserData.isPresent()) {
            ObaUserDataItem userData = optUserData.get();
            userData.setCity(cityName);
            userData.setStopId(stopId);
            userData.setRegionId(region.getId());
            userData.setRegionName(region.getName());
            userData.setObaBaseUrl(region.getObaBaseUrl());
            userData.setPreviousResponse(outText);
            userData.setLastAccessTime(System.currentTimeMillis());
            obaDao.saveUserData(userData);
        } else {
            ObaUserDataItem userData = new ObaUserDataItem(
                    session.getUser().getUserId(),
                    cityName,
                    stopId,
                    region.getId(),
                    region.getName(),
                    region.getObaBaseUrl(),
                    outText,
                    System.currentTimeMillis(),
                    null
            );
            obaDao.saveUserData(userData);
        }

        PlainTextOutputSpeech out = new PlainTextOutputSpeech();
        out.setText(outText);
        return SpeechletResponse.newTellResponse(out);
    }

    private SpeechletResponse askForCity(Optional<String> currentCityName) {
        PlainTextOutputSpeech out = new PlainTextOutputSpeech();
        if (!currentCityName.isPresent()) {
            out.setText("Welcome to OneBusAway! Let's set you up. " +
                    "You'll need your city and your stop number. " +
                    "The stop number is shown on the placard in the bus zone, " +
                    "on your transit agency's web site, " +
                    "or in your OneBusAway mobile app. " +
                    "In what city do you live?");
        } else {
            String intro = "OneBusAway could not locate a OneBusAway " +
                    "region near %s, the city you gave. ";
            String question = "Tell me again, what's the largest city near you?";
            try {
                String allRegions = allRegionsSpoken();
                out.setText(String.format(intro +
                        "Supported regions include %s. " +
                        question,
                        currentCityName.get(),
                        allRegions
                ));
            } catch (IOException e) {
                log.error("Error getting all regions: " + e);
                out.setText(String.format(intro + question, currentCityName.get()));
            }
        }
        return SpeechletResponse.newAskResponse(out, cityReprompt);
    }

    private String allRegionsSpoken() throws IOException {
        List<String> activeRegions = obaClient.getAllRegions()
                .stream()
                .filter(r -> RegionUtils.isRegionUsable(r)
                        && !r.getExperimental()
                        && r.getObaBaseUrl() != null)
                .map(r -> String.format("%s, ", r.getName()))
                .sorted()
                .collect(Collectors.toList());
        // Some low-level manipulation to beautify the sequence of regions.
        int lastElem = activeRegions.size()-1;
        // remove final comma
        activeRegions.set(lastElem, activeRegions.get(lastElem).replaceFirst(", $", ""));
        if (activeRegions.size() > 1) {
            activeRegions.add(lastElem, "and ");
        }

        String finalStr = activeRegions.stream().collect(Collectors.joining(""));
        log.debug("All regions spoken: " + finalStr);
        return finalStr;
    }

    private SpeechletResponse goodbye() {
        String output = String.format("Good-bye");
        PlainTextOutputSpeech out = new PlainTextOutputSpeech();
        out.setText(output);
        return SpeechletResponse.newTellResponse(out);
    }
}
