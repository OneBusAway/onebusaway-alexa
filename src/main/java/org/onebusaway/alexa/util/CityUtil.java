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
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import lombok.extern.log4j.Log4j;
import org.onebusaway.alexa.SessionAttribute;
import org.onebusaway.alexa.lib.ObaClient;
import org.onebusaway.io.client.elements.ObaRegion;
import org.onebusaway.io.client.util.RegionUtils;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.onebusaway.alexa.SessionAttribute.*;

/**
 * Utilities for setting up the region/city for a OneBusAway Alexa user
 */
@Log4j
public class CityUtil {

    public static long NEW_YORK_REGION_ID = 2;  // From http://regions.onebusaway.org/regions-v3.json

    /**
     * Returns the response required to start the initial dialog with the user to select their city/region
     *
     * @param currentCityName the city name the user requested, or null if the user didn't say a city/region name
     * @return the response required to start the initial dialog with the user to select their city/region
     */
    public static SpeechletResponse askForCity(Optional<String> currentCityName, ObaClient obaClient, Session session) {
        boolean experimentalRegions = (boolean) session.getAttribute(EXPERIMENTAL_REGIONS);
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
                String allRegions = allRegionsSpoken(obaClient.getAllRegions(experimentalRegions), experimentalRegions);
                out.setText(String.format(intro +
                                allRegions +
                                question,
                        currentCityName.get()));
            } catch (IOException e) {
                log.error("Error getting all regions: " + e);
                out.setText(String.format(intro + question, currentCityName.get()));
            }
        }
        return SpeechletResponse.newAskResponse(out, SpeechUtil.getCityReprompt());
    }

    /**
     * Returns the text for listing all supported regions to the user
     *
     * @param regions a list of all ObaRegions
     * @param includeExperimentalRegions true if experimental (beta) regions should be included, false if they should not
     * @return the text for listing all supported regions to the user
     * @throws IOException
     */
    public static String allRegionsSpoken(List<ObaRegion> regions, boolean includeExperimentalRegions) {
        List<String> activeRegions = regions
                .stream()
                .filter(r -> (RegionUtils.isRegionUsable(r) || (r.getId() == NEW_YORK_REGION_ID && includeExperimentalRegions))
                        && (!r.getExperimental() || includeExperimentalRegions)
                        && r.getObaBaseUrl() != null)
                .map(r -> String.format("%s, ", SpeechUtil.formatRegionName(r.getName())))
                .sorted()
                .collect(Collectors.toList());
        // Some low-level manipulation to beautify the sequence of regions.
        int lastElem = activeRegions.size() - 1;
        // remove final comma
        activeRegions.set(lastElem, activeRegions.get(lastElem).replaceFirst(", $", ""));
        if (activeRegions.size() > 1) {
            activeRegions.add(lastElem, "and ");
        }

        String finalStr = activeRegions.stream().collect(Collectors.joining(""));
        log.debug("All regions spoken: " + finalStr);
        return String.format("Supported regions include %s. ", finalStr);
    }

    /**
     * Returns the response to the user to ask for a city when the user hasn't provided it yet, but did provide a stop number
     *
     * @param spokenStopNumber stop number that the user said (corresponds to stop_code)
     * @param session
     * @return the response to the user to ask for a city when the user hasn't provided it yet, but did provide a stop number
     */
    public static SpeechletResponse askForCityAfterStop(String spokenStopNumber, Session session) {
        session.setAttribute(STOP_ID, spokenStopNumber);
        session.setAttribute(ASK_STATE, SessionAttribute.AskState.STOP_BEFORE_CITY.toString());
        PlainTextOutputSpeech citySpeech = new PlainTextOutputSpeech();
        citySpeech.setText(String.format("You haven't set your region yet. In what city is stop %s?", spokenStopNumber));

        return SpeechletResponse.newAskResponse(citySpeech, SpeechUtil.getCityReprompt());
    }
}
