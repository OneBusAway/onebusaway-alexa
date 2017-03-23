/*
 * Copyright 2017 Sean J. Barbeau (sjbarbeau@gmail.com)
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
package org.onebusaway.alexa.util;

import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import lombok.extern.log4j.Log4j;
import org.onebusaway.alexa.SessionAttribute;
import org.onebusaway.alexa.lib.GoogleMaps;
import org.onebusaway.alexa.lib.ObaClient;
import org.onebusaway.alexa.lib.ObaUserClient;
import org.onebusaway.alexa.storage.ObaDao;
import org.onebusaway.io.client.elements.ObaRegion;
import org.onebusaway.io.client.elements.ObaStop;
import org.onebusaway.location.Location;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Optional;

import static org.onebusaway.alexa.SessionAttribute.*;

/**
 * Utilities to support the dialog with the user for selecting a stop, including given multiple stops with the same stop_code
 */
@Log4j
public class StopUtil {

    /**
     * Returns the text re-asking for a stop number after a user tried to provide one, but lookup failed
     *
     * @return the text re-asking for a stop number after a user tried to provide one, but lookup failed
     */
    public static SpeechletResponse reaskForStopNumber() {
        PlainTextOutputSpeech out = new PlainTextOutputSpeech();
        out.setText("OneBusAway could not locate your stop number.  Tell me again, what is your stop number?");
        return SpeechletResponse.newAskResponse(out, SpeechUtil.getStopNumReprompt());
    }

    /**
     * Handles the response from the user when responding to the duplicate stop dialog, including determining if the user
     * needs to be asked again or if we've found the stop they are interested in, and returning the proper response to user
     *
     * @param session
     * @param stopFound
     * @param googleMaps
     * @param obaClient
     * @param obaDao
     * @return the proper response to the user for the duplicate stop dialog (e.g., did we find the stop, or do we need to ask again)
     * @throws SpeechletException
     */
    public static SpeechletResponse handleDuplicateStopResponse(Session session, boolean stopFound, GoogleMaps googleMaps, ObaClient obaClient, ObaDao obaDao) throws SpeechletException {
        ArrayList<ObaStop> stops = (ArrayList<ObaStop>) session.getAttribute(DIALOG_FOUND_STOPS);
        boolean experimentalRegions = (boolean) session.getAttribute(EXPERIMENTAL_REGIONS);
        if (stops != null) {
            if (stopFound && stops.size() > 0) {
                String cityName = (String) session.getAttribute(CITY_NAME);

                Optional<Location> location = googleMaps.geocode(cityName);
                if (!location.isPresent()) {
                    return CityUtil.askForCity(Optional.of(cityName), obaClient, session);
                }

                Optional<ObaRegion> region;
                try {
                    region = obaClient.getClosestRegion(location.get(), experimentalRegions);
                } catch (IOException e) {
                    log.error("Error getting closest region: " + e.getMessage());
                    return CityUtil.askForCity(Optional.of(cityName), obaClient, session);
                }

                ObaUserClient obaUserClient;
                try {
                    obaUserClient = obaClient.withObaBaseUrl(region.get().getObaBaseUrl());
                } catch (URISyntaxException e) {
                    log.error("ObaBaseUrl " + region.get().getObaBaseUrl() + " for " + region.get().getName()
                            + " is invalid: " + e.getMessage());
                    // Region didn't have a valid URL - ask again and hopefully we find a different one
                    return CityUtil.askForCity(Optional.of(cityName), obaClient, session);
                }

                LinkedHashMap<String, String> stopData = (LinkedHashMap<String, String>) stops.get(0);
                return StorageUtil.finishOnboard(session, cityName, stopData.get("id"), stopData.get("stopCode"), region.get(), obaUserClient, obaDao);
            } else if (!stopFound && stops.size() > 1) {
                stops.remove(0);
                session.setAttribute(DIALOG_FOUND_STOPS, stops);
                return StopUtil.askUserAboutDuplicateStops(session, null);
            }
        }

        return reaskForStopNumber();
    }

    /**
     * Starts the duplicate stop dialog with the usesr, returns the appropriate dialog message to the user for selecting
     * the proper stop, given duplicate stop_codes for stops passed in as ObaStop[] (if this is the first question to
     * the user in this dialog), or stored in the session attribute DIALOG_FOUND_STOPS (if we've already asked the user this once)
     *
     * @param session contains stops in DIALOG_FOUND_STOPS, if we've already asked the user about a stop in this dialog
     * @param stops   the array of stops with duplicate stop_codes, if we haven't yet asked the user about a stop in this dialog, or null if we've already asked
     * @return the appropriate dialog message to the user for selecting the proper stop, given the state of the dialog drive by the parameters
     */
    public static SpeechletResponse askUserAboutDuplicateStops(Session session, ObaStop[] stops) {
        PlainTextOutputSpeech askForVerifyStop = new PlainTextOutputSpeech();
        String stopName = "";

        if (stops != null && stops.length > 0) {
            session.setAttribute(DIALOG_FOUND_STOPS, stops);
            stopName = stops[0].getName();
            askForVerifyStop.setText(String.format("We found %d stops associated with the stop number. Did you mean the %s stop?", stops.length, stopName));
        } else {
            ArrayList<ObaStop> foundStops = (ArrayList<ObaStop>) session.getAttribute(DIALOG_FOUND_STOPS);
            LinkedHashMap<String, String> stopData = (LinkedHashMap<String, String>) foundStops.get(0);
            stopName = stopData.get("name");
            askForVerifyStop.setText(String.format("Ok, what about the %s stop?", stopName));
        }

        PlainTextOutputSpeech verifyStopSpeech = new PlainTextOutputSpeech();
        verifyStopSpeech.setText(String.format("Did you mean the %s stop?", stopName));
        Reprompt verifyStopReprompt = new Reprompt();
        verifyStopReprompt.setOutputSpeech(verifyStopSpeech);

        session.setAttribute(ASK_STATE, SessionAttribute.AskState.VERIFYSTOP.toString());
        return SpeechletResponse.newAskResponse(askForVerifyStop, verifyStopReprompt);
    }

}
