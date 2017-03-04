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
package org.onebusaway.alexa;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.*;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.log4j.Log4j;
import org.apache.http.util.TextUtils;
import org.onebusaway.alexa.lib.ObaUserClient;
import org.onebusaway.alexa.storage.ObaDao;
import org.onebusaway.alexa.storage.ObaUserDataItem;
import org.onebusaway.alexa.util.SpeechUtil;
import org.onebusaway.io.client.elements.ObaRoute;
import org.onebusaway.io.client.request.ObaArrivalInfoResponse;
import org.onebusaway.io.client.request.ObaStopResponse;
import org.onebusaway.io.client.util.UIUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

import static org.onebusaway.alexa.ObaIntent.*;
import static org.onebusaway.alexa.SessionAttribute.*;
import static org.onebusaway.alexa.lib.ObaUserClient.ARRIVALS_SCAN_MINS;

@NoArgsConstructor
@Log4j
public class AuthedSpeechlet implements Speechlet {

    @Resource
    private ObaDao obaDao;

    @Resource
    private AnonSpeechlet anonSpeechlet;

    private ObaUserClient obaUserClient;

    private ObaUserDataItem userData;

    public void setUserData(@NonNull ObaUserDataItem userData) throws URISyntaxException {
        this.userData = userData;
        this.obaUserClient = new ObaUserClient(userData.getObaBaseUrl());
    }

    @Override
    public SpeechletResponse onIntent(final IntentRequest request,
                                      final Session session)
            throws SpeechletException {
        SpeechUtil.populateAttributes(session, userData);
        AskState askState = SpeechUtil.getAskState(session);
        session.setAttribute(ASK_STATE, AskState.NONE.toString());

        Intent intent = request.getIntent();
        if (HELP.equals(intent.getName())) {
            PlainTextOutputSpeech out = new PlainTextOutputSpeech();
            out.setText("The OneBusAway skill will tell you upcoming transit arrivals " +
            "at a stop of your choice.  You've already configured your region and stop, " +
            "so just open the skill or ask me for arrivals. " +
                    "You can also ask me to change your city or stop." +
                    "I can also tell you times in a clock format such as 10:25 PM.  You can enable this by saying enable clock times, and disable it by saying disable clock times.");
            return SpeechletResponse.newTellResponse(out);
        } else if (REPEAT.equals(intent.getName())) {
            PlainTextOutputSpeech out = new PlainTextOutputSpeech();
            out.setText(getCachedResponse(session));
            return SpeechletResponse.newTellResponse(out);
        } else if (SET_CITY.equals(intent.getName())) {
            return anonSpeechlet.onIntent(request, session);
        } else if (GET_CITY.equals(intent.getName())) {
            return getCity();
        } else if (SET_STOP_NUMBER.equals(intent.getName())) {
            return anonSpeechlet.onIntent(request, session);
        } else if (YES.equals(intent.getName())) {
            return handleYesIntent(request, session, askState);
        } else if (NO.equals(intent.getName())) {
            return handleNoIntent(request, session, askState);
        } else if (GET_STOP_NUMBER.equals(intent.getName())) {
            return getStopDetails();
        } else if (GET_ARRIVALS.equals(intent.getName())) {
            return tellArrivals(session);
        } else if (ENABLE_CLOCK_TIME.equals(intent.getName())) {
            return enableClockTime(session);
        } else if (DISABLE_CLOCK_TIME.equals(intent.getName())) {
            return disableClockTime(session);
        } else if (SET_ROUTE_FILTER.equals(intent.getName())) {
            return setRouteFilter(session);
        } else if (STOP.equals(intent.getName()) || CANCEL.equals(intent.getName())) {
            return SpeechUtil.goodbye();
        } else {
            throw new SpeechletException("Did not recognize intent name");
        }
    }

    @Override
    public SpeechletResponse onLaunch(final LaunchRequest request,
                                      final Session session)
            throws SpeechletException {
        SpeechUtil.populateAttributes(session, userData);
        return tellArrivals(session);
    }

    @Override
    public void onSessionStarted(final SessionStartedRequest request,
                                 final Session session) {
    }

    @Override
    public void onSessionEnded(final SessionEndedRequest request,
                               final Session session) {
    }

    private SpeechletResponse getCity() {
        String output = String.format("You live in %s, near the %s region.", userData.getCity(), userData.getRegionName());
        saveOutputForRepeat(output);
        PlainTextOutputSpeech out = new PlainTextOutputSpeech();
        out.setText(output);
        return SpeechletResponse.newTellResponse(out);
    }

    private SpeechletResponse getStopDetails() throws SpeechletException {
        ObaStopResponse stop = null;
        try {
            stop = obaUserClient.getStopDetails(userData.getStopId());
        } catch (IOException e) {
            throw new SpeechletException(e);
        }
        String output = String.format("Your stop is %s, %s.", stop.getStopCode(), stop.getName());
        saveOutputForRepeat(output);
        PlainTextOutputSpeech out = new PlainTextOutputSpeech();
        out.setText(output);
        return SpeechletResponse.newTellResponse(out);
    }

    private SpeechletResponse tellArrivals(Session session) throws SpeechletException {
        ObaArrivalInfoResponse response;
        try {
            response = obaUserClient.getArrivalsAndDeparturesForStop(
                    userData.getStopId(),
                    ARRIVALS_SCAN_MINS
            );
        } catch (IOException e) {
            throw new SpeechletException(e);
        }

        String timeZoneText = userData.getTimeZone();
        log.debug("time zone is " + timeZoneText);
        TimeZone timeZone = null;
        if (!TextUtils.isEmpty(timeZoneText)) {
            timeZone = TimeZone.getTimeZone(timeZoneText);
        }

        HashSet routesToFilter = SpeechUtil.getRoutesToFilter(obaDao, session);

        String output = SpeechUtil.getArrivalText(response.getArrivalInfo(), ARRIVALS_SCAN_MINS,
                response.getCurrentTime(), userData.getSpeakClockTime(), timeZone, routesToFilter);

        log.info("Full text output: " + output);
        saveOutputForRepeat(output);
        PlainTextOutputSpeech out = new PlainTextOutputSpeech();
        out.setText(output);
        return SpeechletResponse.newTellResponse(out);
    }

    private SpeechletResponse enableClockTime(Session session) throws SpeechletException {
        return updateClockTime(1, session);

    }

    private SpeechletResponse disableClockTime(Session session) throws SpeechletException {
        return updateClockTime(0, session);
    }

    /**
     * Update if clock times should be announced to the user or not (if not, ETAs are used)
     *
     * @param enableClockTime true if clock times should be enabled, false if they should be disabled
     */
    private SpeechletResponse updateClockTime(long enableClockTime, Session session) throws SpeechletException {
        TimeZone timeZone;
        try {
            timeZone = obaUserClient.getTimeZone();
        } catch (IOException e) {
            throw new SpeechletException(e);
        }

        // Update DAO
        userData.setSpeakClockTime(enableClockTime);
        userData.setTimeZone(timeZone.getID());
        obaDao.saveUserData(userData);

        // Update session
        session.setAttribute(CLOCK_TIME, enableClockTime);
        session.setAttribute(TIME_ZONE, timeZone.getID());

        String output = String.format("Clock times are now %s", enableClockTime == 1 ? "enabled" : "disabled");
        saveOutputForRepeat(output);
        PlainTextOutputSpeech out = new PlainTextOutputSpeech();
        out.setText(output);
        return SpeechletResponse.newTellResponse(out);
    }

    private void saveOutputForRepeat(String output) {
        log.debug("Caching output for repeat = " + output);
        userData.setPreviousResponse(output);
        userData.setLastAccessTime(System.currentTimeMillis());
        obaDao.saveUserData(userData);
    }

    /**
     * User asked to filter routes for the currently selected stop, so return the initial response
     *
     * @param session
     * @return the initial response for setting a route filter
     */
    private SpeechletResponse setRouteFilter(final Session session) {
        // Make sure we clear any existing routes to filter for this session (but leave any persisted)
        session.setAttribute(DIALOG_ROUTES_TO_FILTER, null);

        String stopId = (String) session.getAttribute(STOP_ID);
        String regionName = (String) session.getAttribute(REGION_NAME);
        log.debug(String.format(
                "Asked to set a route filter for stop ID %s in region %s...", stopId, regionName));

        ObaStopResponse response;
        try {
            response = obaUserClient.getStop(stopId);
        } catch (IOException e) {
            log.error("Error getting details for stop + " + stopId + " in region " + regionName + ": " + e.getMessage());
            return SpeechUtil.getCommunicationErrorMessage();
        }

        List<ObaRoute> routes = response.getRoutes();
        if (routes.size() <= 1) {
            String output = String.format("There is only one route for this stop, so I can't filter out any routes.");
            saveOutputForRepeat(output);
            PlainTextOutputSpeech out = new PlainTextOutputSpeech();
            out.setText(output);
            return SpeechletResponse.newTellResponse(out);
        }
        session.setAttribute(STOP_CODE, response.getStopCode());

        // There is more than one route - ask user which they want to hear arrivals for
        return askToFilterRoute(session, routes);
    }

    /**
     * Ask the user if they want to hear arrivals for the first route, from the provided list of routes
     *
     * @param session
     * @param routes  list of routes from which the first route will be taken
     * @return response to be read to the user asking if they want to hear arrivals for the first route in the provided list of routes
     */
    private SpeechletResponse askToFilterRoute(Session session, List<ObaRoute> routes) {
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

    private SpeechletResponse handleYesIntent(final IntentRequest request,
                                              final Session session, AskState askState) throws SpeechletException {
        if (askState == AskState.VERIFYSTOP) {
            // User confirmed that they want to select a particular stop - pass to anonSpeechlet to finish dialog
            // Restore the session ASK_STATE
            session.setAttribute(ASK_STATE, askState.toString());
            return anonSpeechlet.onIntent(request, session);
        }

        if (askState == AskState.FILTER_INDIVIDUAL_ROUTE) {
            return handleFilterIndividualRoute(session, true);
        }

        log.error("Received yes intent without a question.");
        return anonSpeechlet.askForCity(Optional.empty());
    }

    private SpeechletResponse handleNoIntent(final IntentRequest request,
                                             Session session, AskState askState) throws SpeechletException {
        if (askState == AskState.VERIFYSTOP) {
            // User said no to the current stop, so ask about the next one - pass to anonSpeechlet to finish dialog
            // Restore the session ASK_STATE
            session.setAttribute(ASK_STATE, askState.toString());
            return anonSpeechlet.onIntent(request, session);
        }

        if (askState == AskState.FILTER_INDIVIDUAL_ROUTE) {
            return handleFilterIndividualRoute(session, false);
        }

        log.error("Received no intent without a question.");
        return anonSpeechlet.askForCity(Optional.empty());
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
    private SpeechletResponse handleFilterIndividualRoute(Session session, boolean hearArrivals) throws SpeechletException {
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
            return askToFilterRoute(session, null);
        }

        // We've asked about all routes for this stop, so persist the route filter
        String stopId = (String) session.getAttribute(STOP_ID);
        HashMap<String, HashSet<String>> persistedRouteFilter = userData.getRoutesToFilter();
        if (persistedRouteFilter == null) {
            persistedRouteFilter = new HashMap<>();
        }
        // Build HashSet from ArrayList (apparently Alexa sessions don't support HashSets directly)
        HashSet<String> routesToFilterHashSet = new HashSet<>();
        for (String routeId : routesToFilter) {
            routesToFilterHashSet.add(routeId);
        }
        persistedRouteFilter.put(stopId, routesToFilterHashSet);
        userData.setRoutesToFilter(persistedRouteFilter);
        obaDao.saveUserData(userData);

        String stopCode = (String) session.getAttribute(STOP_CODE);
        String output = String.format("Alright, I've saved your route filter for stop %s.", stopCode);
        saveOutputForRepeat(output);
        PlainTextOutputSpeech out = new PlainTextOutputSpeech();
        out.setText(output);
        return SpeechletResponse.newTellResponse(out);
    }

    /**
     * Gets a previously cached response to repeat to the user
     * @param session
     * @return a previously cached response to repeat to the user, or a friendly error message
     * if there isn't anything to repeat.
     */
    private String getCachedResponse(Session session) {
        // Try session first
        String lastOutput = (String) session.getAttribute(PREVIOUS_RESPONSE);
        if (!TextUtils.isEmpty(lastOutput)) {
            log.debug("Repeating last output from session = " + lastOutput);
            return lastOutput;
        }
        // Try persisted data
        lastOutput = userData.getPreviousResponse();
        if (!TextUtils.isEmpty(lastOutput)) {
            log.debug("Repeating last output from obaDao = " + lastOutput);
            return lastOutput;
        }
        return "I'm sorry, I don't have anything to repeat.  You can ask me for arrival times for your stop.";
    }
}
