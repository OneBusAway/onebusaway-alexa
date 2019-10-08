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
 * Stores OneBusAway prompts and re-prompts string IDs associated with ResourceBundle.
 */
public enum Prompt {
    //skill prompts
    WELCOME_MESSAGE("welcomeMessage", true),
    FOUND_CITY("foundCity", true),
    CANNOT_LOCATE_CITY("cannotLocateCity", true),
    FINISH_ONBOARDING("finishOnboarding", true),
    INTRODUCTION("introduction"),
    ANNOUNCE_FEATURE("announceFeature"),
    GET_CITY("getCity", true),
    GET_CITY_IN_SESSION_EMPTY("getCityInSessionEmpty", true),
    GET_CITY_IN_SESSION_EXIST("getCityInSessionExist", true),
    GET_STOP("getStop", true),
    LOOKING_FOR_STOP_NUMBER("lookingForStopNumber"),
    REASK_FOR_STOP("reaskForStop"),
    ENABLE_CLOCK_TIME("enableClockTime", true),
    DISABLE_CLOCK_TIME("disableClockTime", true),
    ARRIVAL_INFO_FORMAT("arrivalInfoFormat", true),
    NO_ARRIVALS("noArrivals", true),
    NO_ARRIVALS_AFTER_FILTER("noArrivalsAfterFilter", true),
    SET_FILTER("setFilter", true),
    SELECT_ROUTE_TO_FILTER("selectRouteToFilter"),
    ONLY_ONE_ROUTE("onlyOneRoute", true),
    SAVE_ROUTE_FILTER("saveRouteFilter", true),
    ENABLE_EXPERIMENTAL_REGIONS("enableExperimentalRegions", true),
    DISABLE_EXPERIMENTAL_REGIONS("disableExperimentalRegions", true),
    USER_EXIT("userExit", true),
    HELP_MESSAGE("helpMessage", true),
    ASK_FOR_CITY_AFTER_STOP("askForCityAfterStop", true),
    DUPLICATED_STOPS("duplicatedStops"),
    DUPLICATED_STOP_CONFIRM("duplicatedStopConfirm"),
    NO_REPEAT("noRepeat", true),
    UNKNOWN_INTENT_MESSAGE("unknownIntentMessage"),
    GENERAL_ERROR_MESSAGE("generalErrorMessage"),
    COMMUNICATION_ERROR_MESSAGE("communicationErrorMessage"),

    //skill reprompts
    ASK_FOR_CITY("askForCity"),
    ASK_FOR_STOP("askForStop"),
    VERIFY_STOP("verifyStop"),
    VERIFY_FILTER("verifyFilter");

    private static final String PERSONALIZED_SUFFIX = ".personalized";

    private String resourceId;

    /**
     * Flag to tell if personalized prompt is available or not. The place holder for person name
     * will be added to personalized prompt as the first parameter, so the prompt helper will using this flag to tell if it need to
     * pass the first name as parameter to construct the string.
     */
    private boolean isPersonalizedPrompt;

    Prompt(final String resourceId) {
        this.resourceId = resourceId;
        this.isPersonalizedPrompt = false;
    }

    Prompt(String resourceId, boolean isPersonalizedPrompt) {
        this.resourceId = resourceId;
        this.isPersonalizedPrompt = isPersonalizedPrompt;
    }

    /**
     * Gets the prompt resource ID.
     *
     * @return
     */
    public String getResourceId() {
        return resourceId;
    }

    /**
     * Checks whether the prompt is personalized or not.
     *
     * @return
     */
    public boolean isPersonalizedPrompt() {
        return isPersonalizedPrompt;
    }

    /**
     * Gets the personalized prompt resource ID.
     *
     * @return
     */
    public String getPersonalizedResourceId() {
        if (this.isPersonalizedPrompt) {
            return getResourceId() + PERSONALIZED_SUFFIX;
        } else {
            return getResourceId();
        }
    }
}
