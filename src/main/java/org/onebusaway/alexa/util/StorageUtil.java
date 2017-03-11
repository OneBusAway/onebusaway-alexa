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
import lombok.extern.log4j.Log4j;
import org.apache.http.util.TextUtils;
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

import static org.onebusaway.alexa.SessionAttribute.*;
import static org.onebusaway.alexa.lib.ObaUserClient.ARRIVALS_SCAN_MINS;

/**
 * Utilities that help with persisting data to storage
 */
@Log4j
public class StorageUtil {

    /**
     * Finishes the on-board process of saving user data to the persistent data store and returns response to user saying
     * save was successful
     *
     * @param session
     * @param cityName
     * @param stopId
     * @param stopCode
     * @param region
     * @param obaUserClient
     * @param obaDao
     * @return response to user saying initial save an onboard process was successful
     * @throws SpeechletException
     */
    public static SpeechletResponse finishOnboard(Session session, String cityName, String stopId, String stopCode, ObaRegion region, ObaUserClient obaUserClient, ObaDao obaDao) throws SpeechletException {
        log.debug(String.format(
                "Crupdating user with city %s and stop ID %s, code %s, regionId %d, regionName %s, obaBaseUrl %s.",
                cityName, stopId, stopCode, region.getId(), region.getName(), region.getObaBaseUrl()));
        // Save the current Stop ID to the session so it can be used to pull the route filter
        session.setAttribute(STOP_ID, stopId);

        ObaArrivalInfoResponse response;
        try {
            response = obaUserClient.getArrivalsAndDeparturesForStop(
                    stopId,
                    ARRIVALS_SCAN_MINS
            );
        } catch (IOException e) {
            throw new SpeechletException(e);
        }

        Object speakClockTimeSessionObject = session.getAttribute(CLOCK_TIME);
        Long speakClockTime = 0L;
        if (speakClockTimeSessionObject instanceof Integer) {
            // This happens if it's never been set before - ignore it
        } else if (speakClockTimeSessionObject instanceof Long) {
            speakClockTime = (Long) speakClockTimeSessionObject;
        }

        TimeZone timeZone;
        try {
            timeZone = obaUserClient.getTimeZone();
        } catch (IOException e) {
            throw new SpeechletException(e);
        }

        // This code path is current used for the SetCityIntent if this isn't the users first time using the skill
        // And, we can't store HashMaps in sessions (they get converted to ArrayLists by Alexa)
        // So, try to get route filters from persisted data in case the user has previously set them
        HashSet<String> routesToFilter = SpeechUtil.getRoutesToFilter(obaDao, session);

        String arrivalInfoText = SpeechUtil.getArrivalText(response.getArrivalInfo(), ARRIVALS_SCAN_MINS,
                response.getCurrentTime(), speakClockTime, timeZone, routesToFilter);

        log.info("Full arrival text output: " + arrivalInfoText);

        // Build the full text response to the user
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("Ok, your stop number is %s in the %s region. ", stopCode, region.getName()));
        builder.append(SpeechUtil.getIntroductionText(session));
        builder.append(String.format("Right now, %s", arrivalInfoText));
        String outText = builder.toString();

        createOrUpdateUser(session, cityName, stopId, region.getId(), region.getName(), region.getObaBaseUrl(), outText,
                System.currentTimeMillis(), speakClockTime, timeZone, 1L,
                1L, obaDao);

        PlainTextOutputSpeech out = new PlainTextOutputSpeech();
        out.setText(outText);
        return SpeechletResponse.newTellResponse(out);
    }

    /**
     * Creates or updates the user data in the persistent data store
     *
     * @param session
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
    public static void createOrUpdateUser(Session session, String cityName, String stopId, long regionId, String regionName,
                                          String regionObaBaseUrl, String previousResponse, long lastAccessTime,
                                          long speakClockTime, TimeZone timeZone, long announcedIntroduction, long announcedFeaturesv1_1_0, ObaDao obaDao) {
        Optional<ObaUserDataItem> optUserData = obaDao.getUserData(session);
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
            userData.setAnnouncedFeaturesv1_1_0(announcedFeaturesv1_1_0);
            obaDao.saveUserData(userData);
        } else {
            ObaUserDataItem userData = new ObaUserDataItem(
                    session.getUser().getUserId(),
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

    /**
     * Gets a previously cached response to repeat to the user
     *
     * @param session
     * @return a previously cached response to repeat to the user, or a friendly error message
     * if there isn't anything to repeat.
     */
    public static String getCachedResponse(Session session, ObaUserDataItem userDataItem) {
        // Try session first
        String lastOutput = (String) session.getAttribute(PREVIOUS_RESPONSE);
        if (!TextUtils.isEmpty(lastOutput)) {
            log.debug("Repeating last output from session = " + lastOutput);
            return lastOutput;
        }
        // Try persisted data
        lastOutput = userDataItem.getPreviousResponse();
        if (!TextUtils.isEmpty(lastOutput)) {
            log.debug("Repeating last output from obaDao = " + lastOutput);
            return lastOutput;
        }
        return "I'm sorry, I don't have anything to repeat.  You can ask me for arrival times for your stop.";
    }

    /**
     * Update if clock times should be announced to the user or not (if not, ETAs are used)
     *
     * @param enableClockTime true if clock times should be enabled, false if they should be disabled
     * @return a message to the user saying clock times are enabled or disabled, depending on enableClockTime
     */
    public static SpeechletResponse updateClockTime(long enableClockTime, Session session, ObaDao obaDao, ObaUserDataItem obaUserDataItem, ObaUserClient obaUserClient) throws SpeechletException {
        TimeZone timeZone;
        try {
            timeZone = obaUserClient.getTimeZone();
        } catch (IOException e) {
            throw new SpeechletException(e);
        }

        // Update DAO
        obaUserDataItem.setSpeakClockTime(enableClockTime);
        obaUserDataItem.setTimeZone(timeZone.getID());
        obaDao.saveUserData(obaUserDataItem);

        // Update session
        session.setAttribute(CLOCK_TIME, enableClockTime);
        session.setAttribute(TIME_ZONE, timeZone.getID());

        String output = String.format("Clock times are now %s", enableClockTime == 1 ? "enabled" : "disabled");
        StorageUtil.saveOutputForRepeat(output, obaDao, obaUserDataItem);
        PlainTextOutputSpeech out = new PlainTextOutputSpeech();
        out.setText(output);
        return SpeechletResponse.newTellResponse(out);
    }
}
