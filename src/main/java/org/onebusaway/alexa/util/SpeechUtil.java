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
import org.onebusaway.alexa.storage.ObaDao;
import org.onebusaway.alexa.storage.ObaUserDataItem;
import org.onebusaway.io.client.elements.ObaArrivalInfo;
import org.onebusaway.io.client.util.ArrivalInfo;
import org.onebusaway.io.client.util.UIUtils;

import java.util.*;

import static org.onebusaway.alexa.SessionAttribute.STOP_ID;

/**
 * Utilities for speech-related actions
 */
@Log4j
public class SpeechUtil {

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
                String stopId = (String) session.getAttribute(STOP_ID);
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
                getRealtimeVsStaticText() +
                "You can filter out certain routes for the currently selected stop by saying `filter routes`. " +
                "I can also tell you times in a clock format such as 10:25 AM.  You can enable this by saying " +
                "`enable clock times`, and disable it by saying `disable clock times`. " +
                "If you'd like to change your city or stop, say `set my city` or `set my stop`, followed by the city or stop number. " +
                "If you need additional help, please contact me using email at alexa at One Bus Away dot org");
        return SpeechletResponse.newTellResponse(out);
    }

    public static String getRealtimeVsStaticText() {
        return "All predictions are based on real-time information unless they are followed by the words 'according to the schedule'. ";
    }
}
