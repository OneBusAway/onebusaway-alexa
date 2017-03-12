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
package org.onebusaway.alexa.util;

import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import lombok.extern.log4j.Log4j;
import org.apache.http.util.TextUtils;
import org.onebusaway.alexa.SessionAttribute;
import org.onebusaway.alexa.storage.ObaDao;
import org.onebusaway.alexa.storage.ObaUserDataItem;
import org.onebusaway.io.client.elements.ObaArrivalInfo;
import org.onebusaway.io.client.util.ArrivalInfo;
import org.onebusaway.io.client.util.UIUtils;

import java.util.*;

/**
 * Utilities for speech-related actions
 */
@Log4j
public class SpeechUtil {

    private static final String HELP_REAL_TIME_VS_STATIC_TEXT = "All predictions are based on real-time information unless they are followed by the words 'according to the schedule'. ";
    private static final String HELP_CLOCK_TIME_TEXT = "I can also tell you times in a clock format such as 10:25 AM.  You can enable this by saying " +
            "`enable clock times`. ";
    private static final String HELP_FILTER_ROUTES_TEXT = "You can ask me to filter out certain routes for the currently selected stop by saying `filter routes`. ";

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

    /**
     * Returns the reprompt for setting a city
     *
     * @return the reprompt for setting a city
     */
    public static Reprompt getCityReprompt() {
        return cityReprompt;
    }

    /**
     * Returns the reprompt for setting a stop number
     *
     * @return
     */
    public static Reprompt getStopNumReprompt() {
        return stopNumReprompt;
    }

    /**
     * Format the arrival info for speach
     * @param arrivals arrival information
     * @param arrivalScanMins number of minutes ahead that the arrival information was requested for
     * @param currentTime the time when this arrival information was generated
     * @param routesToFilter a set of routeIds for routes that should NOT be read to the user
     * @return the arrival info text formatted for speech
     */
    public static String getArrivalText(ObaArrivalInfo[] arrivals, int arrivalScanMins, long currentTime, long clockTime, TimeZone timeZone, HashSet<String> routesToFilter) {
        String output;

        boolean clockTimeBool = false;
        if (clockTime == 1) {
            clockTimeBool = true;
        }

        String noArrivals = "There are no upcoming arrivals at your stop for the next " + arrivalScanMins + " minutes";

        if (arrivals.length == 0) {
            output = noArrivals;
        } else {
            List<ArrivalInfo> arrivalInfo = ArrivalInfo.convertObaArrivalInfo(arrivals, null,
                    currentTime, false, clockTimeBool, timeZone);
            final String SEPARATOR = " -- "; // with pause between sentences
            output = UIUtils.getArrivalInfoSummary(arrivalInfo, SEPARATOR, clockTimeBool, timeZone, routesToFilter);
            if (TextUtils.isEmpty(output)) {
                // If all currently running routes were filtered out, provide no arrivals message
                output = noArrivals + ", although arrivals for some routes are currently filtered out.";
            }
            log.info("ArrivalInfo: " + output);
        }
        return output;
    }

    /**
     * Returns the set of routes to filter for the given user and stop ID saved to the provided session or DAO
     * (in that order), or null if there is no filter for the given STOP_ID
     *
     * @param obaDao
     * @param session containing STOP_ID
     * @return the set of routes to filter for the provided STOP_ID in the session or DAO for this user, or null if
     * there is no filter for the given STOP_ID
     */
    public static HashSet getRoutesToFilter(ObaDao obaDao, Session session) {
        HashMap<String, HashSet<String>> routeFilters;
        Optional<ObaUserDataItem> optUserData = obaDao.getUserData(session);
        if (optUserData.isPresent()) {
            routeFilters = optUserData.get().getRoutesToFilterOut();
            if (routeFilters != null) {
                String stopId = (String) session.getAttribute(SessionAttribute.STOP_ID);
                if (stopId == null) {
                    // Try to get Stop ID from DAO
                    stopId = optUserData.get().getStopId();
                }
                return routeFilters.get(stopId);
            }
        }
        return null;
    }



    /**
     * Returns the goodbye response
     *
     * @return the goodbye response
     */
    public static SpeechletResponse goodbye() {
        String output = String.format("Good-bye");
        PlainTextOutputSpeech out = new PlainTextOutputSpeech();
        out.setText(output);
        return SpeechletResponse.newTellResponse(out);
    }

    public static SpeechletResponse getGeneralErrorMessage() {
        String output = String.format("Sorry, something went wrong.  Please try it again and it might work.");
        PlainTextOutputSpeech out = new PlainTextOutputSpeech();
        out.setText(output);
        return SpeechletResponse.newTellResponse(out);
    }

    public static SpeechletResponse getCommunicationErrorMessage() {
        String output = String.format("Sorry, something went wrong communicating with your region's OneBusAway server.  " +
                "Please try it again and it might work.");
        PlainTextOutputSpeech out = new PlainTextOutputSpeech();
        out.setText(output);
        return SpeechletResponse.newTellResponse(out);
    }

    public static SpeechletResponse getHelpMessage() {
        PlainTextOutputSpeech out = new PlainTextOutputSpeech();
        out.setText("The One Bus Away skill serves up fresh, real-time transit information " +
                "at a stop of your choice.  You've already configured your city and stop, " +
                "so to hear predictions just open the skill or ask me for arrivals. " +
                HELP_REAL_TIME_VS_STATIC_TEXT +
                HELP_FILTER_ROUTES_TEXT +
                HELP_CLOCK_TIME_TEXT +
                "If you'd like to change your city or stop, say `set my city` or `set my stop`, followed by the city or stop number. " +
                "If you need additional help, please contact me using email at alexa at One Bus Away dot org");
        return SpeechletResponse.newTellResponse(out);
    }

    /**
     * Returns the text that should be read to the user if this is the first time they've opened the skill, or an empty
     * string if they've already heard this info.
     * <p>
     * After the call to this method, session attribute ANNOUNCED_INTRODUCTION will be 1.
     *
     * @param session if session attribute ANNOUNCED_INTRODUCTION is 0 will return intro text, if is 1 will return empty string
     * @return intro text if session attribute ANNOUNCED_INTRODUCTION is 0, or empty string if is 1
     */
    public static String getIntroductionText(Session session) {
        Long introductionText = null;
        Object introductionTextObject = session.getAttribute(SessionAttribute.ANNOUNCED_INTRODUCTION);
        if (introductionTextObject instanceof Integer) {
            // This happens if it's never been set before - ignore it and use default 0 value
            introductionText = 0L;
        } else if (introductionTextObject instanceof Long) {
            introductionText = (Long) introductionTextObject;
        }

        if (introductionText == null || introductionText == 0L) {
            // We haven't told the user about general OBA features yet - update the session and return the text
            session.setAttribute(SessionAttribute.ANNOUNCED_INTRODUCTION, 1L);
            return "Great.  I am ready to tell you about the next bus.  You can always ask me for arrival times " +
                    "by saying 'open One Bus Away', and filter routes for your currently selected stop by saying 'filter routes'. " +
                    HELP_REAL_TIME_VS_STATIC_TEXT +
                    "You can learn more about other features by asking me for help.  ";
        } else {
            return "";
        }
    }

    /**
     * Returns the text that should be read to the user if this is the first time they've used v1.1.0 to tell them about new
     * features, or an empty string if they've already heard this info.
     * <p>
     * After the call to this method, session attribute ANNOUNCED_FEATURES_V1_1_0 will be 1.
     *
     * @param session if session attribute ANNOUNCED_FEATURES_V1_1_0 is 0 will return feature description for v1.1.0, if is 1 will return empty string
     * @return v1.1.0 feature text if session attribute ANNOUNCED_FEATURES_V1_1_0 is 0, or empty string if is 1
     */
    public static String getAnnounceFeaturev1_1_0Text(Session session) {
        Long announcedFeaturesv1_1_0 = null;
        Object announcedFeaturesv1_1_0Object = session.getAttribute(SessionAttribute.ANNOUNCED_FEATURES_V1_1_0);
        if (announcedFeaturesv1_1_0Object instanceof Integer) {
            // This happens if it's never been set before - ignore it and use default 0 value
            announcedFeaturesv1_1_0 = 0L;
        } else if (announcedFeaturesv1_1_0Object instanceof Long) {
            announcedFeaturesv1_1_0 = (Long) announcedFeaturesv1_1_0Object;
        }

        if (announcedFeaturesv1_1_0 == null || announcedFeaturesv1_1_0 == 0L) {
            // We haven't told the user about new v1.1.0 features yet - update the session and return the text
            session.setAttribute(SessionAttribute.ANNOUNCED_FEATURES_V1_1_0, 1L);
            return "Guess what!  I just got back from spring break transportation camp.  Check out what I learned - " +
                    "I think you're going to love it.  " +
                    "If you want me to filter routes for your currently selected stop, say 'filter routes'.  " +
                    HELP_CLOCK_TIME_TEXT +
                    "Right now, ";
        } else {
            return "";
        }
    }
}
