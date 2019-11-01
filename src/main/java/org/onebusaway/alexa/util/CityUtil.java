/*
 * Copyright 2016-2019 Sean J. Barbeau (sjbarbeau@gmail.com),
 * Philip M. White (philip@mailworks.org)
 * Chunzhang Mo (victormocz@gmail.com)
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.TextUtils;
import org.onebusaway.alexa.config.SpringContext;
import org.onebusaway.alexa.exception.OneBusAwayException;
import org.onebusaway.alexa.helper.PromptHelper;
import org.onebusaway.alexa.lib.GoogleMaps;
import org.onebusaway.alexa.lib.ObaClient;
import org.onebusaway.alexa.lib.ObaUserClient;
import org.onebusaway.alexa.storage.ObaDao;
import org.onebusaway.alexa.storage.ObaUserDataItem;
import org.onebusaway.io.client.elements.ObaRegion;
import org.onebusaway.io.client.elements.ObaStop;
import org.onebusaway.io.client.request.ObaArrivalInfoResponse;
import org.onebusaway.io.client.util.RegionUtils;
import org.onebusaway.location.Location;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

import static org.onebusaway.alexa.constant.Prompt.ARRIVAL_INFO_FORMAT;
import static org.onebusaway.alexa.constant.Prompt.ASK_FOR_CITY;
import static org.onebusaway.alexa.constant.Prompt.ASK_FOR_CITY_AFTER_STOP;
import static org.onebusaway.alexa.constant.Prompt.ASK_FOR_STOP;
import static org.onebusaway.alexa.constant.Prompt.CANNOT_LOCATE_CITY;
import static org.onebusaway.alexa.constant.Prompt.COMMUNICATION_ERROR_MESSAGE;
import static org.onebusaway.alexa.constant.Prompt.DUPLICATED_STOPS;
import static org.onebusaway.alexa.constant.Prompt.LOOKING_FOR_STOP_NUMBER;
import static org.onebusaway.alexa.constant.Prompt.REASK_FOR_STOP;
import static org.onebusaway.alexa.constant.Prompt.VERIFY_STOP;
import static org.onebusaway.alexa.constant.Prompt.WELCOME_MESSAGE;
import static org.onebusaway.alexa.constant.SessionAttribute.ASK_STATE;
import static org.onebusaway.alexa.constant.SessionAttribute.AskState.STOP_BEFORE_CITY;
import static org.onebusaway.alexa.constant.SessionAttribute.AskState.VERIFYSTOP;
import static org.onebusaway.alexa.constant.SessionAttribute.CITY_NAME;
import static org.onebusaway.alexa.constant.SessionAttribute.DIALOG_FOUND_STOPS;
import static org.onebusaway.alexa.constant.SessionAttribute.EXPERIMENTAL_REGIONS;
import static org.onebusaway.alexa.constant.SessionAttribute.STOP_ID;
import static org.onebusaway.alexa.lib.ObaUserClient.ARRIVALS_SCAN_MINS;

/**
 * Utilities for setting up the region/city for a OneBusAway Alexa user.
 */
@Log4j
public class CityUtil {

    public static long NEW_YORK_REGION_ID = 2;  // From http://regions.onebusaway.org/regions-v3.json

    private static PromptHelper promptHelper =
            SpringContext.getInstance().getBean("promptHelper", PromptHelper.class);

    /**
     * Returns the text for listing all supported regions to the user.
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
     * Onboarding user if city and stop number is in the AlexaSession.
     *
     * @param userId identifier for the current user
     * @param attributesManager manager to add or remove attribute from Alexa session
     * @param googleMaps client to access Google web APIs
     * @param obaClient client used to access the OBA REST API for a local OBA server
     * @param obaDao OneBusAway data access object
     * @return alexa response based on cityName and stopId
     */
    public static Optional<Response> fulfillCityAndStop(String userId, AttributesManager attributesManager, GoogleMaps googleMaps, ObaClient obaClient, ObaDao obaDao) {
        String cityName = SessionUtil.getSessionAttribute(attributesManager, CITY_NAME, String.class, StringUtils.EMPTY);
        String stopId = SessionUtil.getSessionAttribute(attributesManager, STOP_ID, String.class, StringUtils.EMPTY);
        boolean experimentalRegions = SessionUtil.getSessionAttribute(attributesManager, EXPERIMENTAL_REGIONS, Boolean.class, false);
        log.info(String.format("cityname is %s, stopId is %s", cityName, stopId));

        if (StringUtils.isBlank(cityName)) {
            SessionUtil.addOrUpdateSessionAttribute(attributesManager, ASK_STATE, STOP_BEFORE_CITY.toString());
            return promptHelper.getResponse(
                    promptHelper.getPrompt(ASK_FOR_CITY_AFTER_STOP, stopId), promptHelper.getPrompt(ASK_FOR_CITY));
        }

        if (StringUtils.isBlank(stopId)) {
            return promptHelper.getResponse(LOOKING_FOR_STOP_NUMBER, ASK_FOR_STOP);
        }

        // Map city name to a geographic location - even if we've done this before, we want to refresh the info
        Optional<Location> location = googleMaps.geocode(cityName);
        if (!location.isPresent()) {
            log.error("location is blank");
            return askForCityResponse(cityName, attributesManager, obaClient);
        }

        // Get closest region from geographic location
        Optional<ObaRegion> region = Optional.empty();
        try {
            region = obaClient.getClosestRegion(location.get(), experimentalRegions);
        } catch (IOException e) {
            return askForCityResponse(cityName, attributesManager, obaClient);
        }
        if (!region.isPresent()) {
            return askForCityResponse(cityName, attributesManager, obaClient);
        }

        ObaUserClient obaUserClient;
        try {
            obaUserClient = obaClient.withObaBaseUrl(region.get().getObaBaseUrl());
        } catch (URISyntaxException e) {
            log.error("ObaBaseUrl " + region.get().getObaBaseUrl() + " for " + region.get().getName()
                    + " is invalid: " + e.getMessage());
            // Region didn't have a valid URL - ask again and hopefully we find a different one
            return askForCityResponse(cityName, attributesManager, obaClient);
        }

        ObaStop[] searchResults;
        try {
            searchResults = obaUserClient.getStopFromCode(location.get(), stopId);
        } catch (IOException e) {
            log.error("Couldn't get stop from code " + stopId + ": " + e.getMessage());
            return askForCityResponse();
        }

        if (searchResults.length == 0) {
            log.info("Search result is 0 for stop.");
            return promptHelper.getResponse(REASK_FOR_STOP, ASK_FOR_STOP);
        } else if (searchResults.length > 1) {
            SessionUtil.addOrUpdateSessionAttribute(attributesManager, DIALOG_FOUND_STOPS, searchResults);
            SessionUtil.addOrUpdateSessionAttribute(attributesManager, ASK_STATE, VERIFYSTOP);
            final String speech = promptHelper.getPrompt(DUPLICATED_STOPS, Integer.toString(searchResults.length), SpeechUtil.replaceSpecialCharactersFromAddress(searchResults[0].getName()));
            final String reprompt = promptHelper.getPrompt(VERIFY_STOP);
            return promptHelper.getResponse(speech, reprompt);
        } else {
            return StorageUtil.finishOnboard(userId, cityName, searchResults[0].getId(), searchResults[0].getStopCode(), region.get(), obaUserClient, obaDao, attributesManager);
        }
    }

    /**
     * Tell arrival information with provided user data (stop number, city name, timezone in obaUserDataItem).
     *
     * @param obaUserDataItem OneBusAway User Data item
     * @param obaUserClient client used to access the OBA REST API for a local OBA server
     * @param attributesManager manager to add or remove attribute from Alexa session
     * @param obaDao OneBusAway data access object
     * @return tell arrival response
     */
    public static Optional<Response> tellArrivals(final ObaUserDataItem obaUserDataItem, final ObaUserClient obaUserClient, final AttributesManager attributesManager, final ObaDao obaDao) {
        ObaArrivalInfoResponse response;
        try {
            response = obaUserClient.getArrivalsAndDeparturesForStop(
                    obaUserDataItem.getStopId(),
                    ARRIVALS_SCAN_MINS
            );
            final String timeZoneText = obaUserDataItem.getTimeZone();
            TimeZone timeZone = null;
            if (!TextUtils.isEmpty(timeZoneText)) {
                timeZone = TimeZone.getTimeZone(timeZoneText);
            }

            String stopId = SessionUtil.getSessionAttribute(attributesManager, STOP_ID, String.class);
            HashSet<String> routesToFilter = obaUserDataItem.getRoutesToFilterOut().get(stopId);

            String output = SpeechUtil.getArrivalText(response.getArrivalInfo(), ARRIVALS_SCAN_MINS,
                    response.getCurrentTime(), obaUserDataItem.getSpeakClockTime(), timeZone, routesToFilter);


            // Build the full text response to the user
            StringBuilder builder = new StringBuilder();
            builder.append(SpeechUtil.getAnnounceFeaturev1_1_0Text(attributesManager));
            builder.append(output);

            // Save that we've already read the tutorial info to the user
            obaUserDataItem.setAnnouncedIntroduction(1L);
            obaUserDataItem.setAnnouncedFeaturesv1_1_0(1L);
            obaDao.saveUserData(obaUserDataItem);

            StorageUtil.saveOutputForRepeat(builder.toString(), obaDao, obaUserDataItem);

            return promptHelper.getResponse(promptHelper.getPrompt(ARRIVAL_INFO_FORMAT, output.toString()));
        } catch (IOException e) {
            throw new OneBusAwayException(promptHelper.getPrompt(COMMUNICATION_ERROR_MESSAGE));
        }
    }

    /**
     * Helper method to return ask for city response.
     *
     * @return ask user for city response
     */
    public static Optional<Response> askForCityResponse() {
        return promptHelper.getResponse(WELCOME_MESSAGE, ASK_FOR_CITY);
    }

    /**
     * Helper method to re-ask user for city because the city user giving is not being supported by OneBusAway.
     *
     * @param cityName the name of the city that get from user
     * @param attributesManager manager to add or remove attribute from Alexa session
     * @param obaClient client used to access the OBA REST API for a local OBA server
     * @return ask for city response with city name in it
     */
    public static Optional<Response> askForCityResponse(final String cityName, final AttributesManager attributesManager, final ObaClient obaClient) {
        if (StringUtils.isBlank(cityName)) {
            return askForCityResponse();
        }
        boolean experimentalRegions = SessionUtil.getSessionAttribute(attributesManager, EXPERIMENTAL_REGIONS, Boolean.class, false);
        try {
            final String allRegions = CityUtil.allRegionsSpoken(obaClient.getAllRegions(experimentalRegions), experimentalRegions);
            return promptHelper.getResponse(promptHelper.getPrompt(CANNOT_LOCATE_CITY, cityName, allRegions), promptHelper.getPrompt(ASK_FOR_CITY));
        } catch (Exception e) {
            return promptHelper.getResponse(promptHelper.getPrompt(CANNOT_LOCATE_CITY, cityName, StringUtils.EMPTY), promptHelper.getPrompt(ASK_FOR_CITY));
        }
    }
}
