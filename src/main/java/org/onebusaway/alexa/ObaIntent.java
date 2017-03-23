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
 * Intents for OneBusAway Alexa.  Must match values in /interaction model/schema.json and utterances.txt.
 */
public class ObaIntent {
    public static final String SET_CITY = "SetCityIntent";
    public static final String GET_CITY = "GetCityIntent";
    public static final String SET_STOP_NUMBER = "SetStopNumberIntent";
    public static final String GET_STOP_NUMBER = "GetStopNumberIntent";
    public static final String GET_ARRIVALS = "GetArrivalsIntent";
    public static final String ENABLE_CLOCK_TIME = "EnableClockTime";
    public static final String DISABLE_CLOCK_TIME = "DisableClockTime";
    public static final String SET_ROUTE_FILTER = "SetRouteFilter";
    public static final String ENABLE_EXPERIMENTAL_REGIONS = "EnableExperimentalRegions";
    public static final String DISABLE_EXPERIMENTAL_REGIONS = "DisableExperimentalRegions";
    public static final String HELP = "AMAZON.HelpIntent";
    public static final String REPEAT = "AMAZON.RepeatIntent";
    public static final String STOP = "AMAZON.StopIntent";
    public static final String CANCEL = "AMAZON.CancelIntent";
    public static final String YES = "AMAZON.YesIntent";
    public static final String NO = "AMAZON.NoIntent";
}
