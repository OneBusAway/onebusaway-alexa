/*
 * Copyright 2017 Sean J. Barbeau (sjbarbeau@gmail.com)
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
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import org.onebusaway.alexa.storage.ObaDao;
import org.onebusaway.alexa.storage.ObaUserDataItem;
import org.onebusaway.io.client.elements.ObaRoute;
import org.onebusaway.io.client.util.UIUtils;

import java.util.*;

import static org.onebusaway.alexa.SessionAttribute.*;

/**
 * Utilities related to allowing the user to filter arrivals spoken for a stop by route
 */
public class RouteFilterUtil {

    /**
     * Ask the user if they want to hear arrivals for the first route, from the provided list of routes
     *
     * @param session
     * @param routes  list of routes from which the first route will be taken
     * @return response to be read to the user asking if they want to hear arrivals for the first route in the provided list of routes
     */
    public static SpeechletResponse askUserAboutFilterRoute(Session session, List<ObaRoute> routes) {
        PlainTextOutputSpeech askForRouteFilter = new PlainTextOutputSpeech();
        String stopCode = (String) session.getAttribute(STOP_CODE);
        String routeName;

        if (routes != null && routes.size() > 0) {
            session.setAttribute(DIALOG_ROUTES_TO_ASK_ABOUT, routes);
            routeName = UIUtils.getRouteDisplayName(routes.get(0));
            askForRouteFilter.setText(String.format("Sure, let's set up a route filter for stop %s.  Do you want to hear arrivals for Route %s?", stopCode, routeName));
        } else {
            ArrayList<ObaRoute> routesToAskAbout = (ArrayList<ObaRoute>) session.getAttribute(DIALOG_ROUTES_TO_ASK_ABOUT);
            LinkedHashMap<String, String> routeData = (LinkedHashMap<String, String>) routesToAskAbout.get(0);
            routeName = UIUtils.getRouteDisplayName(routeData.get("shortName"), routeData.get("longName"));
            askForRouteFilter.setText(String.format("Ok, how about Route %s?", routeName));
        }

        Reprompt askForRouteFilterReprompt = new Reprompt();
        PlainTextOutputSpeech repromptText = new PlainTextOutputSpeech();
        repromptText.setText(String.format("Did you want to hear arrivals for %s for stop %s?", routeName, stopCode));
        askForRouteFilterReprompt.setOutputSpeech(repromptText);

        session.setAttribute(ASK_STATE, AskState.FILTER_INDIVIDUAL_ROUTE.toString());
        return SpeechletResponse.newAskResponse(askForRouteFilter, askForRouteFilterReprompt);
    }

    /**
     * User responded saying they did (hearArrivals==true) or did not (hearArrivals==false) want to hear arrivals for a
     * particular stop (STOP_ID of session) and a paricular route (the 0 index route in the SessionAttribute
     * DIALOG_ROUTES_TO_ASK_ABOUT ArrayList)
     *
     * @param session
     * @param hearArrivals true if the user wanted to hear arrivals about the 0 index route, false if they did not
     * @return
     * @throws SpeechletException
     */
    public static SpeechletResponse handleFilterRouteResponse(Session session, boolean hearArrivals, ObaDao obaDao, ObaUserDataItem obaUserDataItem) throws SpeechletException {
        ArrayList<ObaRoute> routes = (ArrayList<ObaRoute>) session.getAttribute(DIALOG_ROUTES_TO_ASK_ABOUT);
        if (routes == null) {
            // Something went wrong
            return SpeechUtil.getGeneralErrorMessage();
        }
        ArrayList<String> routesToFilter = (ArrayList<String>) session.getAttribute(DIALOG_ROUTES_TO_FILTER);
        if (routesToFilter == null) {
            routesToFilter = new ArrayList<>();
        }

        // Get the last route we asked about - there should be at least one
        LinkedHashMap<String, String> routeData = (LinkedHashMap<String, String>) routes.get(0);

        if (!hearArrivals) {
            // The user doesn't want to hear arrivals for this route, so add the routeId to set of route filters
            routesToFilter.add(routeData.get("id"));
            session.setAttribute(DIALOG_ROUTES_TO_FILTER, routesToFilter);
        }

        // Remove route we just asked about, so we can ask about the next one (if there are any left)
        routes.remove(0);

        if (routes.size() > 0) {
            // Ask about the next route
            session.setAttribute(DIALOG_ROUTES_TO_ASK_ABOUT, routes);
            return askUserAboutFilterRoute(session, null);
        }

        // We've asked about all routes for this stop, so persist the route filter
        String stopId = (String) session.getAttribute(STOP_ID);
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

        String stopCode = (String) session.getAttribute(STOP_CODE);
        String output = String.format("Alright, I've saved your route filter for stop %s.", stopCode);
        StorageUtil.saveOutputForRepeat(output, obaDao, obaUserDataItem);
        PlainTextOutputSpeech out = new PlainTextOutputSpeech();
        out.setText(output);
        return SpeechletResponse.newTellResponse(out);
    }
}
