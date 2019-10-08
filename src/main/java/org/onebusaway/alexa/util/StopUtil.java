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

import com.amazon.ask.attributes.AttributesManager;
import com.amazon.ask.model.Response;
import lombok.extern.log4j.Log4j;
import org.onebusaway.alexa.config.SpringContext;
import org.onebusaway.alexa.constant.SessionAttribute;
import org.onebusaway.alexa.helper.PromptHelper;
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

import static org.onebusaway.alexa.constant.Prompt.ASK_FOR_STOP;
import static org.onebusaway.alexa.constant.Prompt.DUPLICATED_STOPS;
import static org.onebusaway.alexa.constant.Prompt.DUPLICATED_STOP_CONFIRM;
import static org.onebusaway.alexa.constant.Prompt.REASK_FOR_STOP;
import static org.onebusaway.alexa.constant.Prompt.VERIFY_STOP;
import static org.onebusaway.alexa.constant.SessionAttribute.ASK_STATE;
import static org.onebusaway.alexa.constant.SessionAttribute.CITY_NAME;
import static org.onebusaway.alexa.constant.SessionAttribute.DIALOG_FOUND_STOPS;
import static org.onebusaway.alexa.helper.PromptHelper.ADDRESS_SSML_FORMAT;
import static org.onebusaway.alexa.util.SpeechUtil.removeSpecialCharactersFromAddress;

/**
 * Utilities to support the dialog with the user for selecting a stop, including given multiple stops with the same stop_code.
 */
@Log4j
public class StopUtil {

    private static PromptHelper promptHelper =
            SpringContext.getInstance().getBean("promptHelper", PromptHelper.class);

    /**
     * Handles the response from the user when responding to the duplicate stop dialog, including determining if the user
     * needs to be asked again or if we've found the stop they are interested in, and returning the proper response to user
     *
     * @param userId
     * @param attributesManager
     * @param stopFound
     * @param googleMaps
     * @param obaClient
     * @param obaDao
     * @return the proper response to the user for the duplicate stop dialog (e.g., did we find the stop, or do we need to ask again)
     */
    public static Optional<Response> handleDuplicateStopResponse(String userId, AttributesManager attributesManager, boolean stopFound, GoogleMaps googleMaps, ObaClient obaClient, ObaDao obaDao) {
        ArrayList<ObaStop> stops = SessionUtil.getSessionAttribute(attributesManager, DIALOG_FOUND_STOPS, ArrayList.class);
        boolean experimentalRegions = SessionUtil.getSessionAttribute(attributesManager, SessionAttribute.EXPERIMENTAL_REGIONS, Boolean.class, false);
        if (stops != null) {
            if (stopFound && stops.size() > 0) {
                String cityName = SessionUtil.getSessionAttribute(attributesManager, CITY_NAME, String.class);

                Optional<Location> location = googleMaps.geocode(cityName);
                if (!location.isPresent()) {
                    return CityUtil.askForCityResponse(cityName, attributesManager, obaClient);
                }

                Optional<ObaRegion> region;
                try {
                    region = obaClient.getClosestRegion(location.get(), experimentalRegions);
                } catch (IOException e) {
                    log.error("Error getting closest region: " + e.getMessage());
                    return CityUtil.askForCityResponse(cityName, attributesManager, obaClient);
                }

                ObaUserClient obaUserClient;
                try {
                    obaUserClient = obaClient.withObaBaseUrl(region.get().getObaBaseUrl());
                } catch (URISyntaxException e) {
                    log.error("ObaBaseUrl " + region.get().getObaBaseUrl() + " for " + region.get().getName()
                            + " is invalid: " + e.getMessage());
                    // Region didn't have a valid URL - ask again and hopefully we find a different one
                    return CityUtil.askForCityResponse(cityName, attributesManager, obaClient);
                }

                LinkedHashMap<String, String> stopData = (LinkedHashMap<String, String>) stops.get(0);
                return StorageUtil.finishOnboard(userId, cityName, stopData.get("id"), stopData.get("stopCode"), region.get(), obaUserClient, obaDao, attributesManager);
            } else if (!stopFound && stops.size() > 1) {
                stops.remove(0);
                SessionUtil.addOrUpdateSessionAttribute(attributesManager, DIALOG_FOUND_STOPS, stops);
                return StopUtil.askUserAboutDuplicateStops(attributesManager, null);
            }
        }

        return promptHelper.getResponse(REASK_FOR_STOP, ASK_FOR_STOP);
    }

    /**
     * Starts the duplicate stop dialog with the usesr, returns the appropriate dialog message to the user for selecting
     * the proper stop, given duplicate stop_codes for stops passed in as ObaStop[] (if this is the first question to
     * the user in this dialog), or stored in the session attribute DIALOG_FOUND_STOPS (if we've already asked the user this once)
     *
     * @param attributesManager manager to add or remove attribute from Alexa session
     * @param stops             the array of stops with duplicate stop_codes, if we haven't yet asked the user about a stop in this dialog, or null if we've already asked
     * @return the appropriate dialog message to the user for selecting the proper stop, given the state of the dialog drive by the parameters
     */
    public static Optional<Response> askUserAboutDuplicateStops(AttributesManager attributesManager, ObaStop[] stops) {
        String stopName = "";
        String speech = "";
        if (stops != null && stops.length > 0) {
            SessionUtil.addOrUpdateSessionAttribute(attributesManager, DIALOG_FOUND_STOPS, stops);
            stopName = String.format(ADDRESS_SSML_FORMAT, removeSpecialCharactersFromAddress(stops[0].getName()));
            speech = promptHelper.getPrompt(DUPLICATED_STOPS, Integer.toString(stops.length), stopName);
        } else {
            ArrayList<ObaStop> foundStops = SessionUtil.getSessionAttribute(attributesManager, DIALOG_FOUND_STOPS, ArrayList.class);
            LinkedHashMap<String, String> stopData = (LinkedHashMap<String, String>) foundStops.get(0);
            stopName = String.format(ADDRESS_SSML_FORMAT, removeSpecialCharactersFromAddress(stopData.get("name")));
            speech = promptHelper.getPrompt(DUPLICATED_STOP_CONFIRM, stopName);
        }
        String reprompt = promptHelper.getPrompt(VERIFY_STOP, stopName);
        SessionUtil.addOrUpdateSessionAttribute(attributesManager, ASK_STATE, SessionAttribute.AskState.VERIFYSTOP.toString());
        return promptHelper.getResponse(speech, reprompt);
    }

}
