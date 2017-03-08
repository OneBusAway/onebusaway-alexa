/*
 * Copyright 2016-2017 Philip M. White (philip@mailworks.org),
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
import lombok.extern.log4j.Log4j;
import org.onebusaway.alexa.lib.GoogleMaps;
import org.onebusaway.alexa.lib.ObaClient;
import org.onebusaway.alexa.lib.ObaUserClient;
import org.onebusaway.alexa.storage.ObaDao;
import org.onebusaway.alexa.util.*;
import org.onebusaway.io.client.elements.ObaRegion;
import org.onebusaway.io.client.elements.ObaStop;
import org.onebusaway.io.client.util.RegionUtils;
import org.onebusaway.location.Location;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;

import static org.onebusaway.alexa.ObaIntent.*;
import static org.onebusaway.alexa.SessionAttribute.*;

@Log4j
public class AnonSpeechlet implements Speechlet {

    @Resource
    private ObaDao obaDao;

    @Resource
    private GoogleMaps googleMaps;

    @Resource
    private ObaClient obaClient;

    @Override
    public void onSessionStarted(SessionStartedRequest sessionStartedRequest, Session session) throws SpeechletException {
    }

    @Override
    public SpeechletResponse onLaunch(LaunchRequest launchRequest, Session session) throws SpeechletException {
        switch (SessionUtil.getOnboardState(session)) {
            case Fresh:
                return CityUtil.askForCity(Optional.empty(), obaClient);
            case OnlyCity:
                return StopUtil.reaskForStopNumber();
            default:
                throw new SpeechletException("Unrecognized onboard state");
        }
    }

    @Override
    public SpeechletResponse onIntent(IntentRequest request, Session session)
            throws SpeechletException {
        Intent intent = request.getIntent();
        AskState askState = SessionUtil.getAskState(session);
        session.setAttribute(ASK_STATE, AskState.NONE.toString());
        if (HELP.equals(intent.getName()) ||
                GET_ARRIVALS.equals(intent.getName()) ||
                GET_STOP_NUMBER.equals(intent.getName()) ||
                ENABLE_CLOCK_TIME.equals(intent.getName()) ||
                DISABLE_CLOCK_TIME.equals(intent.getName()) ||
                SET_ROUTE_FILTER.equals(intent.getName()) ||
                REPEAT.equals(intent.getName())) {
            // User asked for help, or we don't yet have enough information to respond.  Return welcome message.
            return CityUtil.askForCity(Optional.empty(), obaClient);
        } else if (SET_CITY.equals(intent.getName())) {
            String cityName = intent.getSlot(CITY_NAME).getValue();
            //if we're at STOP_BEFORE_CITY, preserve state until we're sure we have a region
            session.setAttribute(ASK_STATE, askState.toString());
            if (cityName == null) {
                return CityUtil.askForCity(Optional.empty(), obaClient);
            }

            Optional<Location> location = googleMaps.geocode(cityName);
            if (!location.isPresent()) {
                // Couldn't find the city at all.
                return CityUtil.askForCity(Optional.of(cityName), obaClient);
            }

            // Get closest region from geographic location
            Optional<ObaRegion> region;
            try {
                region = obaClient.getClosestRegion(location.get());
            } catch (IOException e) {
                log.error("Error getting closest region: " + e.getMessage());
                return CityUtil.askForCity(Optional.of(cityName), obaClient);
            }

            if (!region.isPresent() || region.get().getObaBaseUrl() == null) {
                // Couldn't find a nearby region that supports the OBA REST API
                return CityUtil.askForCity(Optional.of(cityName), obaClient);
            } else {
                // Got a region!
                session.setAttribute(CITY_NAME, cityName);
                session.setAttribute(REGION_ID, region.get().getId());
                session.setAttribute(REGION_NAME, region.get().getName());
                session.setAttribute(OBA_BASE_URL, region.get().getObaBaseUrl());
                //no longer need to preserve STOP_BEFORE_CITY state
                session.setAttribute(ASK_STATE, AskState.NONE.toString());

                if (askState == AskState.STOP_BEFORE_CITY) {
                    return setStopNumber(
                            (String) session.getAttribute(STOP_ID),
                            session);
                } else {
                    PlainTextOutputSpeech out = new PlainTextOutputSpeech();
                    out.setText(String.format("Ok, we found the %s region near you.  What's your stop number?",
                            region.get().getName()));
                    return SpeechletResponse.newAskResponse(out, SpeechUtil.getStopNumReprompt());
                }
            }
        } else if (GET_CITY.equals(intent.getName())) {
            String city = (String)session.getAttribute(CITY_NAME);
            if (city == null) {
                PlainTextOutputSpeech out = new PlainTextOutputSpeech();
                out.setText("You have not yet told me where you live.  What is your city?");
                return SpeechletResponse.newAskResponse(out, SpeechUtil.getCityReprompt());
            } else {
                PlainTextOutputSpeech out = new PlainTextOutputSpeech();
                out.setText(
                        String.format("You just told me you live in %s, " +
                                        "but we still need your stop number. " +
                                        "What's your stop number?",
                                city));
                return SpeechletResponse.newAskResponse(out, SpeechUtil.getStopNumReprompt());
            }
        } else if (SET_STOP_NUMBER.equals(intent.getName())) {
            String stopNumberStr = intent.getSlot(STOP_ID).getValue();
            if (stopNumberStr == null) {
                PlainTextOutputSpeech out = new PlainTextOutputSpeech();
                out.setText("What is your stop number?");
                return SpeechletResponse.newAskResponse(out, SpeechUtil.getStopNumReprompt());
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
            return SpeechUtil.goodbye();
        } else {
            throw new SpeechletException("Did not recognize intent name");
        }
    }

    @Override
    public void onSessionEnded(SessionEndedRequest sessionEndedRequest, Session session) throws SpeechletException {

    }

    private SpeechletResponse handleYesIntent(Session session, AskState askState) throws SpeechletException {
        if (askState == AskState.VERIFYSTOP) {
            return StopUtil.handleDuplicateStopResponse(session, true, googleMaps, obaClient, obaDao);
        }

        log.error("Received yes intent without a question.");
        return CityUtil.askForCity(Optional.empty(), obaClient);
    }

    private SpeechletResponse handleNoIntent(Session session, AskState askState) throws SpeechletException {
        if (askState == AskState.VERIFYSTOP) {
            return StopUtil.handleDuplicateStopResponse(session, false, googleMaps, obaClient, obaDao);
        }

        log.error("Received no intent without a question.");
        return CityUtil.askForCity(Optional.empty(), obaClient);
    }

    private SpeechletResponse setStopNumber(String spokenStopNumber, Session session) throws SpeechletException {
        String cityName = (String) session.getAttribute(CITY_NAME);
        String regionName = (String) session.getAttribute(REGION_NAME);
        log.debug(String.format(
                "Asked to set stop number %s in city %s for region %s...", spokenStopNumber, cityName, regionName));
        if (cityName == null) {
            return CityUtil.askForCityAfterStop(spokenStopNumber, session);
        }

        // Map city name to a geographic location - even if we've done this before, we want to refresh the info
        Optional<Location> location = googleMaps.geocode(cityName);
        if (!location.isPresent()) {
            return CityUtil.askForCity(Optional.of(cityName), obaClient);
        }

        // Get closest region from geographic location
        Optional<ObaRegion> region;
        try {
            region = obaClient.getClosestRegion(location.get());
        } catch (IOException e) {
            log.error("Error getting closest region: " + e.getMessage());
            return CityUtil.askForCity(Optional.of(cityName), obaClient);
        }
        if (!region.isPresent() || !RegionUtils.isRegionUsable(region.get())) {
            // Couldn't find a nearby region that supports the OBA REST API
            return CityUtil.askForCity(Optional.of(cityName), obaClient);
        }

        ObaUserClient obaUserClient;
        try {
            obaUserClient = obaClient.withObaBaseUrl(region.get().getObaBaseUrl());
        } catch (URISyntaxException e) {
            log.error("ObaBaseUrl " + region.get().getObaBaseUrl() + " for " + region.get().getName()
                    + " is invalid: " + e.getMessage());
            // Region didn't have a valid URL - ask again and hopefully we find a different one
            return CityUtil.askForCity(Optional.of(cityName), obaClient);
        }

        ObaStop[] searchResults;
        try {
            searchResults = obaUserClient.getStopFromCode(location.get(), spokenStopNumber);
        } catch (IOException e) {
            log.error("Couldn't get stop from code " + spokenStopNumber + ": " + e.getMessage());
            return StopUtil.reaskForStopNumber();
        }

        if (searchResults.length == 0) {
            return StopUtil.reaskForStopNumber();
        } else if (searchResults.length > 1) {
            return StopUtil.askUserAboutDuplicateStops(session, searchResults);
        } else {
            // Perfect!
            return finishOnboard(session, cityName, searchResults[0], region.get(), obaUserClient);
        }
    }

    private SpeechletResponse finishOnboard(Session session,
                                            String cityName,
                                            ObaStop stop,
                                            ObaRegion region,
                                            ObaUserClient obaUserClient) throws SpeechletException {
        return StorageUtil.finishOnboard(session, cityName, stop.getId(), stop.getStopCode(), region, obaUserClient, obaDao);
    }
}
