/*
 * Copyright 2016 Sean J. Barbeau (sjbarbeau@gmail.com),
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
import lombok.extern.log4j.Log4j;
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

        if (arrivals.length == 0) {
            output = "There are no upcoming arrivals at your stop for the next "
                    + arrivalScanMins + " minutes.";
        } else {
            List<ArrivalInfo> arrivalInfo = ArrivalInfo.convertObaArrivalInfo(arrivals, null,
                    currentTime, false, clockTimeBool, timeZone);
            final String SEPARATOR = " -- "; // with pause between sentences
            output = UIUtils.getArrivalInfoSummary(arrivalInfo, SEPARATOR, clockTimeBool, timeZone, routesToFilter);
            log.info("ArrivalInfo: " + output);
        }
        return output;
    }

    /**
     * Returns the set of routes to filter for the given user and stop ID saved to the provided session, or null if
     * there is no filter for the given STOP_ID
     *
     * @param obaDao
     * @param session containing STOP_ID
     * @return the set of routes to filter for the provided STOP_ID in the session for this user, or null if there is no
     * filter for the given STOP_ID
     */
    public static HashSet getRoutesToFilter(ObaDao obaDao, Session session) {
        HashMap<String, HashSet<String>> routeFilters;
        Optional<ObaUserDataItem> optUserData = obaDao.getUserData(session);
        if (optUserData.isPresent()) {
            routeFilters = optUserData.get().getRoutesToFilter();
            if (routeFilters != null) {
                return routeFilters.get((String) session.getAttribute(STOP_ID));
            }
        }
        return null;
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
}
