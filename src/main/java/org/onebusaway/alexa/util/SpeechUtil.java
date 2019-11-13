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
import com.google.common.collect.ImmutableMap;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.TextUtils;
import org.onebusaway.alexa.config.SpringContext;
import org.onebusaway.alexa.constant.Prompt;
import org.onebusaway.alexa.constant.SessionAttribute;
import org.onebusaway.alexa.helper.PromptHelper;
import org.onebusaway.alexa.storage.ObaDao;
import org.onebusaway.alexa.storage.ObaUserDataItem;
import org.onebusaway.io.client.elements.ObaArrivalInfo;
import org.onebusaway.io.client.util.ArrivalInfo;
import org.onebusaway.io.client.util.UIUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

import static org.onebusaway.alexa.constant.Prompt.NO_ARRIVALS;
import static org.onebusaway.alexa.constant.Prompt.NO_ARRIVALS_AFTER_FILTER;
import static org.onebusaway.alexa.constant.SessionAttribute.ANNOUNCED_FEATURES_V1_1_0;

/**
 * Utilities for speech-related actions.
 */
@Log4j
public class SpeechUtil {
    private static PromptHelper promptHelper =
            SpringContext.getInstance().getBean("promptHelper", PromptHelper.class);

    /**
     * Format the arrival info for speach
     *
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

        String noArrivals = promptHelper.getPrompt(NO_ARRIVALS, Integer.toString(arrivalScanMins));

        if (arrivals.length == 0) {
            output = noArrivals;
        } else {
            List<ArrivalInfo> arrivalInfo = ArrivalInfo.convertObaArrivalInfo(arrivals, null,
                    currentTime, false, clockTimeBool, timeZone);
            final String SEPARATOR = " -- "; // with pause between sentences
            output = UIUtils.getArrivalInfoSummary(arrivalInfo, SEPARATOR, clockTimeBool, timeZone, routesToFilter);
            if (TextUtils.isEmpty(output)) {
                // If all currently running routes were filtered out, provide no arrivals message
                output = promptHelper.getPrompt(NO_ARRIVALS_AFTER_FILTER, Integer.toString(arrivalScanMins));
            }
            output = replaceSpecialCharactersFromAddress(output);
            log.info("ArrivalInfo: " + output);
        }
        return output;
    }

    /**
     * Returns the set of routes to filter for the given user and stop ID saved to the provided session or DAO
     * (in that order), or null if there is no filter for the given STOP_ID
     *
     * @param obaDao OneBusAway data access object
     * @param userId partition key to retrieve ObaUserDataItem
     * @param alexaSession alexa skill session
     * @return the set of routes to filter for the provided STOP_ID in the session or DAO for this user, or null if
     * there is no filter for the given STOP_ID
     */
    public static HashSet getRoutesToFilter(ObaDao obaDao, String userId, Map<String, Object> alexaSession) {
        HashMap<String, HashSet<String>> routeFilters;
        Optional<ObaUserDataItem> optUserData = obaDao.getUserData(userId);
        if (optUserData.isPresent()) {
            routeFilters = optUserData.get().getRoutesToFilterOut();
            if (routeFilters != null) {
                String stopId = (String) alexaSession.get(SessionAttribute.STOP_ID);
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
     * Returns the text that should be read to the user if this is the first time they've used v1.1.0 to tell them about new
     * features, or an empty string if they've already heard this info.
     * <p>
     * After the call to this method, session attribute ANNOUNCED_FEATURES_V1_1_0 will be 1.
     *
     * @param attributesManager manager to add or remove attribute from Alexa session
     * @return v1.1.0 feature text if session attribute ANNOUNCED_FEATURES_V1_1_0 is 0, or empty string if is 1
     */
    public static String getAnnounceFeaturev1_1_0Text(AttributesManager attributesManager) {
        long announcedFeaturesv1_1_0 = SessionUtil.getSessionAttribute(attributesManager, ANNOUNCED_FEATURES_V1_1_0, Long.class, 0L);

        if (announcedFeaturesv1_1_0 == 0L) {
            // We haven't told the user about new v1.1.0 features yet - update the session and return the text
            SessionUtil.addOrUpdateSessionAttribute(attributesManager, ANNOUNCED_FEATURES_V1_1_0, 1L);
            return promptHelper.getPrompt(Prompt.ANNOUNCE_FEATURE);
        } else {
            return StringUtils.EMPTY;
        }
    }

    /**
     * Formats a region name for speech, including removing "(beta)" from experimental region names
     *
     * @param regionName region name to format
     * @return a region name formatted for speech, including removing "(beta)" from experimental region names
     */
    public static String formatRegionName(String regionName) {
        return regionName.replace(" (beta)", "");
    }

    /**
     * Util to replace & with 'and', '+' with 'and', '@' with 'at', and replace other special characters with whitespace.
     *
     * @param address the address get from Oba service
     * @return address without symbols
     * @see <a href="https://developer.amazon.com/docs/custom-skills/speech-synthesis-markup-language-ssml-reference.html#supported-symbols"></a>
     */
    public static String replaceSpecialCharactersFromAddress(final String address) {
        ImmutableMap<Character, String> mapping = ImmutableMap.<Character, String>builder()
                .put('&', "and")
                .put('+', "and")
                .put('@', "at")
                .put('.', ".")
                .put(',', ",")
                // pause for 1/4 second.
                .put('-', "<break time='250ms'/>")
                .build();
        StringBuilder stringBuilder = new StringBuilder();
        for (Character c : address.toCharArray()) {
            if (!Character.isAlphabetic(c) && !Character.isDigit(c)) {
                stringBuilder.append(mapping.getOrDefault(c, " "));
            } else {
                stringBuilder.append(c);
            }
        }
        return stringBuilder.toString();
    }
}
