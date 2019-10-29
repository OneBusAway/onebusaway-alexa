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
import com.amazon.ask.model.Response;
import com.amazon.ask.response.ResponseBuilder;
import lombok.extern.log4j.Log4j;
import org.onebusaway.alexa.config.SpringContext;
import org.onebusaway.alexa.helper.PromptHelper;
import org.onebusaway.alexa.storage.ObaDao;
import org.onebusaway.alexa.storage.ObaUserDataItem;
import org.onebusaway.io.client.elements.ObaRoute;
import org.onebusaway.io.client.util.UIUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

import static org.onebusaway.alexa.constant.Prompt.GENERAL_ERROR_MESSAGE;
import static org.onebusaway.alexa.constant.Prompt.SAVE_ROUTE_FILTER;
import static org.onebusaway.alexa.constant.Prompt.SELECT_ROUTE_TO_FILTER;
import static org.onebusaway.alexa.constant.Prompt.SET_FILTER;
import static org.onebusaway.alexa.constant.Prompt.VERIFY_FILTER;
import static org.onebusaway.alexa.constant.SessionAttribute.ASK_STATE;
import static org.onebusaway.alexa.constant.SessionAttribute.AskState;
import static org.onebusaway.alexa.constant.SessionAttribute.DIALOG_ROUTES_TO_ASK_ABOUT;
import static org.onebusaway.alexa.constant.SessionAttribute.DIALOG_ROUTES_TO_FILTER;
import static org.onebusaway.alexa.constant.SessionAttribute.STOP_CODE;
import static org.onebusaway.alexa.constant.SessionAttribute.STOP_ID;

/**
 * Utilities related to allowing the user to filter arrivals spoken for a stop by route.
 */
@Log4j
public class RouteFilterUtil {

    private static PromptHelper promptHelper =
            SpringContext.getInstance().getBean("promptHelper", PromptHelper.class);

    /**
     * Ask the user if they want to hear arrivals for the first route, from the provided list of routes
     *
     * @param attributesManager manager to add or remove attribute from Alexa session
     * @param routes list of routes from which the first route will be taken
     * @return response to be read to the user asking if they want to hear arrivals for the first route in the provided list of routes
     */
    public static Optional<Response> askUserAboutFilterRoute(AttributesManager attributesManager, List<ObaRoute> routes) {
        String stopCode = SessionUtil.getSessionAttribute(attributesManager, STOP_CODE, String.class);
        String routeName;
        String speech = "";
        if (routes != null && routes.size() > 0) {
            SessionUtil.addOrUpdateSessionAttribute(attributesManager, DIALOG_ROUTES_TO_ASK_ABOUT, routes);
            routeName = UIUtils.getRouteDisplayName(routes.get(0));
            speech = promptHelper.getPrompt(SET_FILTER, stopCode, routeName);
        } else {
            ArrayList<ObaRoute> routesToAskAbout = SessionUtil.getSessionAttribute(attributesManager, DIALOG_ROUTES_TO_ASK_ABOUT, ArrayList.class);
            LinkedHashMap<String, String> routeData = (LinkedHashMap<String, String>) routesToAskAbout.get(0);
            routeName = UIUtils.getRouteDisplayName(routeData.get("shortName"), routeData.get("longName"));
            speech = promptHelper.getPrompt(SELECT_ROUTE_TO_FILTER, routeName);
        }

        String reprompt = promptHelper.getPrompt(VERIFY_FILTER, routeName, stopCode);

        SessionUtil.addOrUpdateSessionAttribute(attributesManager, ASK_STATE, AskState.FILTER_INDIVIDUAL_ROUTE.toString());
        return promptHelper.getResponse(speech, reprompt);
    }

    /**
     * User responded saying they did (hearArrivals==true) or did not (hearArrivals==false) want to hear arrivals for a
     * particular stop (STOP_ID of session) and a paricular route (the 0 index route in the SessionAttribute
     * DIALOG_ROUTES_TO_ASK_ABOUT ArrayList)
     *
     * @param attributesManager manager to add or remove attribute from Alexa session
     * @param hearArrivals true if the user wanted to hear arrivals about the 0 index route, false if they did not.
     * @param obaDao OneBusAway data access object
     * @param obaUserDataItem OneBusAway User Data item
     * @return
     */
    public static Optional<Response> handleFilterRouteResponse(AttributesManager attributesManager, boolean hearArrivals, ObaDao obaDao, ObaUserDataItem obaUserDataItem) {
        ResponseBuilder responseBuilder = new ResponseBuilder();
        ArrayList<ObaRoute> routes = SessionUtil.getSessionAttribute(attributesManager, DIALOG_ROUTES_TO_ASK_ABOUT, ArrayList.class);
        if (routes == null) {
            // Something went wrong
            return promptHelper.getResponse(GENERAL_ERROR_MESSAGE);
        }
        ArrayList<String> routesToFilter = SessionUtil.getSessionAttribute(attributesManager, DIALOG_ROUTES_TO_FILTER, ArrayList.class, new ArrayList());

        // Get the last route we asked about - there should be at least one
        LinkedHashMap<String, String> routeData = (LinkedHashMap<String, String>) routes.get(0);

        if (!hearArrivals) {
            // The user doesn't want to hear arrivals for this route, so add the routeId to set of route filters
            routesToFilter.add(routeData.get("id"));
            SessionUtil.addOrUpdateSessionAttribute(attributesManager, DIALOG_ROUTES_TO_FILTER, routesToFilter);
        }

        // Remove route we just asked about, so we can ask about the next one (if there are any left)
        routes.remove(0);

        if (routes.size() > 0) {
            // Ask about the next route
            SessionUtil.addOrUpdateSessionAttribute(attributesManager, DIALOG_ROUTES_TO_ASK_ABOUT, routes);
            return askUserAboutFilterRoute(attributesManager, null);
        }

        // We've asked about all routes for this stop, so persist the route filter
        String stopId = SessionUtil.getSessionAttribute(attributesManager, STOP_ID, String.class);
        HashMap<String, HashSet<String>> persistedRouteFilter = obaUserDataItem.getRoutesToFilterOut();
        if (persistedRouteFilter == null) {
            persistedRouteFilter = new HashMap<>();
        }
        // Build HashSet from ArrayList (apparently Alexa sessions don't support HashSets directly)
        HashSet<String> routesToFilterHashSet = new HashSet<>();
        for (String routeId : routesToFilter) {
            routesToFilterHashSet.add(routeId);
        }
        persistedRouteFilter.put(stopId, routesToFilterHashSet);
        obaUserDataItem.setRoutesToFilterOut(persistedRouteFilter);
        obaDao.saveUserData(obaUserDataItem);

        String stopCode = SessionUtil.getSessionAttribute(attributesManager, STOP_CODE, String.class);
        String speech = promptHelper.getPrompt(SAVE_ROUTE_FILTER, stopCode);
        return responseBuilder.withSpeech(speech)
                .withShouldEndSession(true)
                .build();
    }
}
