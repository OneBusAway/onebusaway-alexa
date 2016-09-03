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
package org.onebusaway.alexa;

/**
 * OneBusAway Alexa session attributes
 */
public class SessionAttribute {
    public static final String CITY_NAME = "cityName";
    public static final String STOP_NUMBER = "stopNumber";
    public static final String REGION_ID = "regionId";
    public static final String REGION_NAME = "regionName";
    public static final String OBA_BASE_URL = "obaBaseUrl";
    public static final String PREVIOUS_RESPONSE = "previousResponse";
    public static final String LAST_ACCESS_TIME = "lastAccessTime";
    public static final String FOUND_STOPS = "foundStops";
    public static final String ASK_STATE = "askState";

    public enum AskState {
        NONE,
        VERIFYSTOP
    }
}
