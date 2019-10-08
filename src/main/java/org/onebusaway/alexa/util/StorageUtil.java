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
import org.apache.commons.lang3.StringUtils;
import org.onebusaway.alexa.OneBusAwayAlexa;
import org.onebusaway.alexa.config.SpringContext;
import org.onebusaway.alexa.exception.OneBusAwayException;
import org.onebusaway.alexa.helper.PromptHelper;
import org.onebusaway.alexa.lib.ObaUserClient;
import org.onebusaway.alexa.storage.ObaDao;
import org.onebusaway.alexa.storage.ObaUserDataItem;
import org.onebusaway.io.client.elements.ObaRegion;
import org.onebusaway.io.client.request.ObaArrivalInfoResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.TimeZone;

import static org.onebusaway.alexa.constant.Prompt.COMMUNICATION_ERROR_MESSAGE;
import static org.onebusaway.alexa.constant.Prompt.FINISH_ONBOARDING;
import static org.onebusaway.alexa.constant.Prompt.GENERAL_ERROR_MESSAGE;
import static org.onebusaway.alexa.constant.Prompt.INTRODUCTION;
import static org.onebusaway.alexa.constant.SessionAttribute.ANNOUNCED_INTRODUCTION;
import static org.onebusaway.alexa.constant.SessionAttribute.CLOCK_TIME;
import static org.onebusaway.alexa.constant.SessionAttribute.EXPERIMENTAL_REGIONS;
import static org.onebusaway.alexa.lib.ObaUserClient.ARRIVALS_SCAN_MINS;

/**
 * Utilities that help with persisting data to storage.
 */
@Log4j
public class StorageUtil {

    private static PromptHelper promptHelper =
            SpringContext.getInstance().getBean("promptHelper", PromptHelper.class);

    /**
     * Finishes the on-board process of saving user data to the persistent data store and returns response to user saying
     * save was successful
     *
     * @param userId
     * @param cityName
     * @param stopId
     * @param stopCode
     * @param region
     * @param obaUserClient
     * @param obaDao
     * @param attributesManager
     * @return response to user saying initial save an onboard process was successful
     */
    public static Optional<Response> finishOnboard(
            String userId, String cityName, String stopId, String stopCode, ObaRegion region,
            ObaUserClient obaUserClient, ObaDao obaDao, AttributesManager attributesManager) {
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
            log.error("Failed to get oba arrival info", e);
            throw new OneBusAwayException(promptHelper.getPrompt(COMMUNICATION_ERROR_MESSAGE));
        }

        int speakClockTime = SessionUtil.getSessionAttribute(attributesManager, CLOCK_TIME, Integer.class, 0);

        TimeZone timeZone;
        try {
            timeZone = obaUserClient.getTimeZone();
        } catch (IOException e) {
            throw new OneBusAwayException(promptHelper.getPrompt(GENERAL_ERROR_MESSAGE));
        }

        boolean experimentalRegions = SessionUtil.getSessionAttribute(attributesManager, EXPERIMENTAL_REGIONS, Boolean.class, false);

        // This code path is current used for the SetCityIntent if this isn't the users first time using the skill
        // And, we can't store HashMaps in sessions (they get converted to ArrayLists by Alexa)
        // So, try to get route filters from persisted data in case the user has previously set them
        HashSet<String> routesToFilter = SpeechUtil.getRoutesToFilter(obaDao, userId, attributesManager.getSessionAttributes());

        String arrivalInfoText = SpeechUtil.getArrivalText(response.getArrivalInfo(), ARRIVALS_SCAN_MINS,
                response.getCurrentTime(), speakClockTime, timeZone, routesToFilter);

        log.info("Full arrival text output: " + arrivalInfoText);

        int announcedIntroduction = SessionUtil.getSessionAttribute(attributesManager, ANNOUNCED_INTRODUCTION, Integer.class, 0);
        String introduction = announcedIntroduction > 0 ?
                StringUtils.EMPTY : promptHelper.getPrompt(INTRODUCTION);

        String onboardingSpeech = promptHelper.getPrompt(FINISH_ONBOARDING, stopCode, SpeechUtil.formatRegionName(region.getName()), introduction, arrivalInfoText);
        createOrUpdateUser(userId, cityName, stopId, region.getId(), region.getName(), region.getObaBaseUrl(), onboardingSpeech,
                System.currentTimeMillis(), speakClockTime, timeZone, 1L,
                1L, experimentalRegions, obaDao);
        return promptHelper.getResponse(onboardingSpeech);
    }

    /**
     * Creates or updates the user data in the persistent data store
     *
     * @param userId
     * @param cityName
     * @param stopId
     * @param regionId
     * @param regionName
     * @param regionObaBaseUrl
     * @param previousResponse
     * @param lastAccessTime
     * @param speakClockTime
     * @param timeZone
     * @param announcedIntroduction
     * @param announcedFeaturesv1_1_0
     * @param obaDao
     */
    public static void createOrUpdateUser(String userId, String cityName, String stopId, long regionId, String regionName,
                                          String regionObaBaseUrl, String previousResponse, long lastAccessTime,
                                          long speakClockTime, TimeZone timeZone, long announcedIntroduction, long announcedFeaturesv1_1_0,
                                          boolean experimentalRegions, ObaDao obaDao) {
        Optional<ObaUserDataItem> optUserData = obaDao.getUserData(userId);
        if (optUserData.isPresent()) {
            ObaUserDataItem userData = optUserData.get();
            userData.setCity(cityName);
            userData.setStopId(stopId);
            userData.setRegionId(regionId);
            userData.setRegionName(regionName);
            userData.setObaBaseUrl(regionObaBaseUrl);
            userData.setPreviousResponse(previousResponse);
            userData.setLastAccessTime(lastAccessTime);
            userData.setSpeakClockTime(speakClockTime);
            userData.setTimeZone(timeZone.getID());
            userData.setAnnouncedIntroduction(announcedIntroduction);
            userData.setAnnouncedFeaturesv1_1_0(announcedFeaturesv1_1_0);
            userData.setExperimentalRegions(experimentalRegions);
            obaDao.saveUserData(userData);
        } else {
            ObaUserDataItem userData = new ObaUserDataItem(
                    userId,
                    cityName,
                    stopId,
                    regionId,
                    regionName,
                    regionObaBaseUrl,
                    previousResponse,
                    lastAccessTime,
                    speakClockTime,
                    timeZone.getID(),
                    new HashMap<>(),
                    announcedIntroduction,
                    announcedFeaturesv1_1_0,
                    experimentalRegions,
                    null
            );
            obaDao.saveUserData(userData);
        }
    }

    /**
     * Saves the provided text as the last text spoken to user, so it can be retrieved if the user asks for a repeat
     *
     * @param output   text to be saved as the last spoken text to user
     * @param userData
     * @param obaDao
     */
    public static void saveOutputForRepeat(String output, ObaDao obaDao, ObaUserDataItem userData) {
        log.debug("Caching output for repeat = " + output);
        userData.setPreviousResponse(output);
        userData.setLastAccessTime(System.currentTimeMillis());
        obaDao.saveUserData(userData);
    }
}
