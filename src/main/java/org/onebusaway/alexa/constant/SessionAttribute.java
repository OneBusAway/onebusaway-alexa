/*
 * Copyright 2016 Sean J. Barbeau (sjbarbeau@gmail.com),
 * Philip M. White (philip@mailworks.org)
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
package org.onebusaway.alexa.constant;

/**
 * OneBusAway Alexa session attributes.
 */
public class SessionAttribute {
    //PRINCIPLE_ID in session can be personId or userId depends which one is available.
    public static final String PRINCIPLE_ID = "principleId";
    public static final String CITY_NAME = "cityName";
    public static final String STOP_ID = "stopNumber";
    public static final String STOP_CODE = "stopCode";
    public static final String REGION_ID = "regionId";
    public static final String REGION_NAME = "regionName";
    public static final String OBA_BASE_URL = "obaBaseUrl";
    public static final String PREVIOUS_RESPONSE = "previousResponse";
    public static final String LAST_ACCESS_TIME = "lastAccessTime";
    public static final String DIALOG_FOUND_STOPS = "foundStops";
    public static final String ASK_STATE = "askState";
    public static final String CLOCK_TIME = "clockTime";
    public static final String TIME_ZONE = "timeZone";
    public static final String DIALOG_ROUTES_TO_ASK_ABOUT = "foundRoutes";
    public static final String DIALOG_ROUTES_TO_FILTER = "dialogRoutesToFilter";
    public static final String ANNOUNCED_INTRODUCTION = "announcedIntroduction";
    public static final String ANNOUNCED_FEATURES_V1_1_0 = "announcedFeaturesv1_1_0";
    public static final String EXPERIMENTAL_REGIONS = "experimentalRegions";

    // Strangely, we can't save HashSets or HashMaps to sessions (Amazon Alexa converts them to ArrayLists, which
    // generates a ClassCastException when trying to retrieve them.  This prevents us from saving route filters to sessions.
    //public static final String ROUTES_TO_FILTER = "routesToFilterOut";

    public enum AskState {
        NONE,
        VERIFYSTOP,
        STOP_BEFORE_CITY,
        FILTER_INDIVIDUAL_ROUTE
    }
}
