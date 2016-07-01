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

import lombok.extern.log4j.Log4j;
import org.onebusaway.io.client.elements.ObaArrivalInfo;
import org.onebusaway.io.client.util.ArrivalInfo;

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
     * @return the arrival info text formatted for speech
     */
    public static String getArrivalText(ObaArrivalInfo[] arrivals, int arrivalScanMins, long currentTime) {
        String output;

        if (arrivals.length == 0) {
            output = "There are no upcoming arrivals at your stop for the next "
                    + arrivalScanMins + " minutes.";
        } else {
            StringBuilder sb = new StringBuilder();
            for (ObaArrivalInfo obaArrival: arrivals) {
                log.info("Arrival: " + obaArrival);
                ArrivalInfo arrival = new ArrivalInfo(obaArrival, currentTime, false);
                sb.append(arrival.getLongDescription() + " -- "); //with pause between sentences
            }
            output = sb.toString();
        }
        return output;
    }
}
