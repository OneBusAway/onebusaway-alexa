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

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.*;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import config.UnitTests;
import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onebusaway.alexa.lib.GoogleMaps;
import org.onebusaway.alexa.lib.ObaClient;
import org.onebusaway.alexa.lib.ObaUserClient;
import org.onebusaway.alexa.storage.ObaDao;
import org.onebusaway.alexa.storage.ObaUserDataItem;
import org.onebusaway.alexa.util.SessionUtil;
import org.onebusaway.io.client.elements.*;
import org.onebusaway.io.client.request.ObaArrivalInfoResponse;
import org.onebusaway.io.client.request.ObaStopResponse;
import org.onebusaway.location.Location;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import util.TestUtil;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;
import static org.onebusaway.alexa.ObaIntent.*;
import static org.onebusaway.alexa.SessionAttribute.*;
import static org.onebusaway.alexa.lib.ObaUserClient.ARRIVALS_SCAN_MINS;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class,
        classes = UnitTests.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AuthedSpeechletTest {
    static final String TEST_USER_ID = "test-user-id";
    static final User testUser = User.builder().withUserId(TEST_USER_ID).build();
    Session session;
    static final LaunchRequest launchRequest = LaunchRequest.builder().withRequestId("test-req-id").build();

    private static final ObaRegion TEST_REGION_1 = new ObaRegionElement(
            1,
            "Tampa Bay",
            true,
            "http://api.tampa.onebusaway.org/api/",
            "test-siri-url",
            new ObaRegionElement.Bounds[0],
            "test-lang",
            "test-contact-email",
            true, true, true,
            "test-twitter",
            false,
            "test-stop-info-url"
    );

    private static final ObaRegion TEST_REGION_2 = new ObaRegionElement(
            2,
            "Puget Sound",
            true,
            "http://test-oba-url.example.com",
            "test-siri-url",
            new ObaRegionElement.Bounds[0],
            "test-lang",
            "test-contact-email",
            true, true, true,
            "test-twitter",
            false,
            "test-stop-info-url"
    );

    private static final ObaRegion TEST_REGION_3 = new ObaRegionElement(
            2,
            "Atlanta",
            true,
            "http://test-oba-url.example.com",
            "test-siri-url",
            new ObaRegionElement.Bounds[0],
            "test-lang",
            "test-contact-email",
            true, true, true,
            "test-twitter",
            false,
            "test-stop-info-url"
    );

    private static final ObaRegion TEST_REGION_EXPERIMENTAL = new ObaRegionElement(
            3,
            "Boston (beta)",
            true,
            "http://test-oba-url.example.com",
            "test-siri-url",
            new ObaRegionElement.Bounds[0],
            "test-lang",
            "test-contact-email",
            true, true, true,
            "test-twitter",
            true,
            "test-stop-info-url"
    );

    @Mocked
    ObaArrivalInfo obaArrivalInfo;

    @Mocked
    ObaStop obaStop;

    @Mocked
    ObaStop obaStop2;

    @Mocked
    ObaRoute obaRoute;

    @Mocked
    ObaRoute obaRoute2;

    @Mocked
    ObaStopResponse obaStopResponse;

    @Mocked
    ObaArrivalInfoResponse obaArrivalInfoResponse;

    @Mocked
    GoogleMaps googleMaps;

    @Mocked
    ObaClient obaClient;

    @Mocked
    ObaUserClient obaUserClient;

    @Mocked
    ObaDao obaDao;

    @Mocked
    ArrayList<ObaRegion> obaRegions;

    @Resource
    AuthedSpeechlet authedSpeechlet;

    @Resource
    ObaUserDataItem testUserData;

    @Before
    public void initializeAuthedSpeechlet() throws URISyntaxException {
        authedSpeechlet.setUserData(testUserData);
    }

    @Before
    public void resetFactoryObjects() {
        session = Session.builder().withUser(testUser).withSessionId("test-session-id").build();
    }

    @Test
    public void getStopDetails(@Mocked ObaStopResponse mockResponse) throws SpeechletException, IOException, URISyntaxException {
        new Expectations() {{
            mockResponse.getStopCode(); result = "6497";
            mockResponse.getName(); result = "University Area Transit Center";
            obaUserClient.getStopDetails(anyString); result = mockResponse;
        }};

        SpeechletResponse sr = authedSpeechlet.onIntent(
                IntentRequest.builder()
                        .withRequestId("test-request-id")
                        .withIntent(
                                Intent.builder()
                                        .withName(GET_STOP_NUMBER)
                                        .withSlots(new HashMap<String, Slot>())
                                        .build()
                        )
                        .build(),
                session
        );

        String spoken = ((PlainTextOutputSpeech)sr.getOutputSpeech()).getText();
        assertThat(spoken, equalTo("Your stop is 6497, University Area Transit Center."));
    }

    @Test
    public void launchTellsArrivals() throws SpeechletException, IOException {
        // Turn off tutorials
        session.setAttribute(ANNOUNCED_INTRODUCTION, 1L);
        session.setAttribute(ANNOUNCED_FEATURES_V1_1_0, 1L);

        ObaArrivalInfo[] obaArrivalInfoArray = new ObaArrivalInfo[1];
        obaArrivalInfoArray[0] = obaArrivalInfo;
        new Expectations() {{
            obaArrivalInfo.getShortName(); result = "8";
            obaArrivalInfo.getHeadsign(); result = "Mlk Way Jr";
            obaArrivalInfoResponse.getArrivalInfo(); result = obaArrivalInfoArray;
            obaUserClient.getArrivalsAndDeparturesForStop(anyString, anyInt); result = obaArrivalInfoResponse;
        }};

        SpeechletResponse sr = authedSpeechlet.onLaunch(
                launchRequest,
                session);

        String spoken = ((PlainTextOutputSpeech)sr.getOutputSpeech()).getText();
        assertThat(spoken, equalTo("Route 8 Mlk Way Jr is departing now based on the schedule -- "));

        // Turn on tutorials
        session.setAttribute(ANNOUNCED_FEATURES_V1_1_0, 0L);
        sr = authedSpeechlet.onLaunch(
                launchRequest,
                session);

        spoken = ((PlainTextOutputSpeech) sr.getOutputSpeech()).getText();
        assertThat(spoken, startsWith("Guess what!  I just got back from"));
    }

    @Test
    public void launchTellsArrivalsFilteredAllRoutes() throws SpeechletException, IOException {
        HashMap<String, HashSet<String>> routeFilters = new HashMap<>();
        String stopId = "Hillsborough Area Regional Transit_100";
        String routeId = "Hillsborough Area Regional Transit_8";
        HashSet<String> routesToFilter = new HashSet<>();
        routesToFilter.add(routeId);
        routeFilters.put(stopId, routesToFilter);
        testUserData.setRoutesToFilterOut(routeFilters);
        testUserData.setStopId(stopId);
        session.setAttribute(ANNOUNCED_INTRODUCTION, 1L);
        session.setAttribute(ANNOUNCED_FEATURES_V1_1_0, 1L);
        obaDao.saveUserData(testUserData);

        ObaArrivalInfo[] obaArrivalInfoArray = new ObaArrivalInfo[1];
        obaArrivalInfoArray[0] = obaArrivalInfo;

        new NonStrictExpectations() {{
            obaArrivalInfo.getStopId();
            result = stopId;
            obaArrivalInfo.getRouteId();
            result = routeId;
            obaArrivalInfo.getShortName();
            result = "8";
            obaArrivalInfo.getHeadsign();
            result = "Mlk Way Jr";
            obaArrivalInfoResponse.getArrivalInfo();
            result = obaArrivalInfoArray;
            obaUserClient.getArrivalsAndDeparturesForStop(anyString, anyInt);
            result = obaArrivalInfoResponse;

            obaDao.getUserData(session);
            result = Optional.of(testUserData);
        }};

        SessionUtil.populateAttributes(session, Optional.of(testUserData));

        SpeechletResponse sr = authedSpeechlet.onLaunch(
                launchRequest,
                session);

        String spoken = ((PlainTextOutputSpeech) sr.getOutputSpeech()).getText();
        assertThat(spoken, equalTo("There are no upcoming arrivals at your stop for the next 65 minutes, although arrivals for some routes are currently filtered out."));
    }

    @Test
    public void setStopNumberTutorial() throws SpeechletException, IOException {
        String newStopCode = "3105";

        // Mock persisted user data
        testUserData.setUserId(TEST_USER_ID);
        testUserData.setStopId("6497");
        testUserData.setCity(TEST_REGION_1.getName());
        testUserData.setRegionName(TEST_REGION_1.getName());
        testUserData.setRegionId(TEST_REGION_1.getId());
        testUserData.setObaBaseUrl(TEST_REGION_1.getObaBaseUrl());
        testUserData.setAnnouncedIntroduction(0L);
        testUserData.setAnnouncedFeaturesv1_1_0(0L);

        // Mock stop info
        ObaStop[] obaStopsArray = new ObaStop[1];
        obaStopsArray[0] = obaStop;

        new NonStrictExpectations() {{
            googleMaps.geocode(TEST_REGION_1.getName());
            Location l = new Location("test");
            l.setLatitude(27.9681);
            l.setLongitude(-82.4764);
            result = Optional.of(l);

            obaStop.getStopCode(); result = newStopCode;
            obaStop.getId(); result = newStopCode;
            obaUserClient.getStopFromCode(l, newStopCode); result = obaStopsArray;

            obaClient.getClosestRegion(l, false);
            result = Optional.of(TEST_REGION_1);

            obaDao.getUserData(session); result = Optional.of(testUserData);
        }};

        HashMap<String, Slot> slots = new HashMap<>();
        slots.put(STOP_ID, Slot.builder()
                .withName(STOP_ID)
                .withValue(newStopCode).build());
        SpeechletResponse sr = authedSpeechlet.onIntent(
                IntentRequest.builder()
                        .withRequestId("test-request-id")
                        .withIntent(
                                Intent.builder()
                                        .withName(SET_STOP_NUMBER)
                                        .withSlots(slots)
                                        .build()
                        )
                        .build(),
                session
        );
        String spoken = ((PlainTextOutputSpeech) sr.getOutputSpeech()).getText();
        assertThat(spoken, startsWith("Ok, your stop number is " + newStopCode + " in the " + TEST_REGION_1.getName() + " region. " +
                "Great.  I am ready to tell you about the next bus."));
    }

    @Test
    public void setStopNumberNoTutorial() throws SpeechletException, IOException {
        String newStopCode = "3105";

        // Mock persisted user data
        testUserData.setUserId(TEST_USER_ID);
        testUserData.setStopId("6497");
        testUserData.setCity(TEST_REGION_1.getName());
        testUserData.setRegionName(TEST_REGION_1.getName());
        testUserData.setRegionId(TEST_REGION_1.getId());
        testUserData.setObaBaseUrl(TEST_REGION_1.getObaBaseUrl());
        testUserData.setAnnouncedIntroduction(1L);
        testUserData.setAnnouncedFeaturesv1_1_0(1L);

        // Mock stop info
        ObaStop[] obaStopsArray = new ObaStop[1];
        obaStopsArray[0] = obaStop;

        new NonStrictExpectations() {{
            googleMaps.geocode(TEST_REGION_1.getName());
            Location l = new Location("test");
            l.setLatitude(27.9681);
            l.setLongitude(-82.4764);
            result = Optional.of(l);

            obaStop.getStopCode();
            result = newStopCode;
            obaStop.getId();
            result = newStopCode;
            obaUserClient.getStopFromCode(l, newStopCode);
            result = obaStopsArray;

            obaClient.getClosestRegion(l, false);
            result = Optional.of(TEST_REGION_1);

            obaDao.getUserData(session);
            result = Optional.of(testUserData);
        }};

        HashMap<String, Slot> slots = new HashMap<>();
        slots.put(STOP_ID, Slot.builder()
                .withName(STOP_ID)
                .withValue(newStopCode).build());
        SpeechletResponse sr = authedSpeechlet.onIntent(
                IntentRequest.builder()
                        .withRequestId("test-request-id")
                        .withIntent(
                                Intent.builder()
                                        .withName(SET_STOP_NUMBER)
                                        .withSlots(slots)
                                        .build()
                        )
                        .build(),
                session
        );
        String spoken = ((PlainTextOutputSpeech) sr.getOutputSpeech()).getText();
        assertThat(spoken, startsWith("Ok, your stop number is " + newStopCode + " in the " + TEST_REGION_1.getName() + " region. Right now"));
    }

    @Test
    public void setRecognizableCityTampa() throws SpeechletException, IOException {
        // Mock persisted user data for Test Region 2
        testUserData.setUserId(TEST_USER_ID);
        testUserData.setStopId("6497");
        testUserData.setCity(TEST_REGION_2.getName());
        testUserData.setRegionName(TEST_REGION_2.getName());
        testUserData.setRegionId(TEST_REGION_2.getId());
        testUserData.setObaBaseUrl(TEST_REGION_2.getObaBaseUrl());

        // Set up change to Test Region 1
        new Expectations() {{
            googleMaps.geocode("Tampa");
            Location l = new Location("test");
            l.setLatitude(27.9681);
            l.setLongitude(-82.4764);
            result = Optional.of(l);

            obaClient.getClosestRegion(l, false);
            result = Optional.of(TEST_REGION_1);
        }};

        HashMap<String, Slot> slots = new HashMap<>();
        slots.put(CITY_NAME, Slot.builder()
                .withName(CITY_NAME)
                .withValue("Tampa").build());
        SpeechletResponse sr = authedSpeechlet.onIntent(
                IntentRequest.builder()
                        .withRequestId("test-request-id")
                        .withIntent(
                                Intent.builder()
                                        .withName(SET_CITY)
                                        .withSlots(slots)
                                        .build()
                        )
                        .build(),
                session
        );
        String spoken = ((PlainTextOutputSpeech)sr.getOutputSpeech()).getText();
        assertThat(spoken, startsWith("Ok, we found the " + TEST_REGION_1.getName() +
                " region near you.  What's your stop number?"));
    }

    @Test
    public void enableClockTimes() throws SpeechletException {
        SpeechletResponse sr = authedSpeechlet.onIntent(
                IntentRequest.builder()
                        .withRequestId("test-request-id")
                        .withIntent(
                                Intent.builder()
                                        .withName(ENABLE_CLOCK_TIME)
                                        .withSlots(new HashMap<String, Slot>())
                                        .build()
                        )
                        .build(),
                session
        );
        String spoken = ((PlainTextOutputSpeech) sr.getOutputSpeech()).getText();
        assertThat(spoken, containsString("Clock times are now enabled"));
        assertEquals((long) session.getAttribute(CLOCK_TIME), 1L);
        assertEquals(testUserData.getSpeakClockTime(), 1L);
    }

    @Test
    public void disableClockTimes() throws SpeechletException {
        SpeechletResponse sr = authedSpeechlet.onIntent(
                IntentRequest.builder()
                        .withRequestId("test-request-id")
                        .withIntent(
                                Intent.builder()
                                        .withName(DISABLE_CLOCK_TIME)
                                        .withSlots(new HashMap<String, Slot>())
                                        .build()
                        )
                        .build(),
                session
        );
        String spoken = ((PlainTextOutputSpeech) sr.getOutputSpeech()).getText();
        assertThat(spoken, containsString("Clock times are now disabled"));
        assertEquals((long) session.getAttribute(CLOCK_TIME), 0L);
        assertEquals(testUserData.getSpeakClockTime(), 0L);
    }

    @Test
    public void help() throws SpeechletException {
        SpeechletResponse sr = authedSpeechlet.onIntent(
                IntentRequest.builder()
                        .withRequestId("test-request-id")
                        .withIntent(
                                Intent.builder()
                                        .withName(HELP)
                                        .withSlots(new HashMap<String, Slot>())
                                        .build()
                        )
                        .build(),
                session
        );
        String spoken = ((PlainTextOutputSpeech)sr.getOutputSpeech()).getText();
        assertThat(spoken, containsString("You've already configured your city and stop"));
    }

    @Test
    public void repeat() throws SpeechletException, URISyntaxException, IOException {
        // Turn off tutorials
        session.setAttribute(ANNOUNCED_INTRODUCTION, 1L);
        session.setAttribute(ANNOUNCED_FEATURES_V1_1_0, 1L);

        ObaArrivalInfo[] obaArrivalInfoArray = new ObaArrivalInfo[1];
        obaArrivalInfoArray[0] = obaArrivalInfo;
        String response = "Route 8 Mlk Way Jr is departing now based on the schedule -- ";

        new NonStrictExpectations() {{
            obaArrivalInfo.getShortName();
            result = "8";
            obaArrivalInfo.getHeadsign();
            result = "Mlk Way Jr";
            obaArrivalInfoResponse.getArrivalInfo();
            result = obaArrivalInfoArray;
            obaUserClient.getArrivalsAndDeparturesForStop(anyString, anyInt);
            result = obaArrivalInfoResponse;
            obaDao.getUserData(session);
            result = Optional.of(testUserData);
        }};

        // Try the repeat intent - this simulates after initial setup, when we don't yet have anything to repeat
        IntentRequest repeatIntent = IntentRequest.builder()
                .withRequestId("test-request-id")
                .withIntent(
                        Intent.builder()
                                .withName(REPEAT)
                                .withSlots(new HashMap<String, Slot>())
                                .build()
                )
                .build();
        SpeechletResponse sr = authedSpeechlet.onIntent(repeatIntent, session);
        String spoken = ((PlainTextOutputSpeech)sr.getOutputSpeech()).getText();
        assertThat(spoken, containsString("I'm sorry, I don't have anything to repeat.  You can ask me for arrival times for your stop."));

        // Test initial request/response - this should also save the response for later retrieval via repeat intent
        sr = authedSpeechlet.onLaunch(launchRequest, session);
        spoken = ((PlainTextOutputSpeech)sr.getOutputSpeech()).getText();
        assertThat(spoken, startsWith(response));

        // Now try repeat intent again, with a fresh session - we should get the last response again
        Session newSession = Session.builder()
                .withUser(testUser)
                .withSessionId("test-session-id2")
                .build();

        sr = authedSpeechlet.onIntent(repeatIntent, newSession);
        spoken = ((PlainTextOutputSpeech)sr.getOutputSpeech()).getText();
        assertThat(spoken, containsString(response));
    }

    @Test
    public void noUpcomingArrivals() throws SpeechletException, IOException {
        // Turn off tutorials
        session.setAttribute(ANNOUNCED_INTRODUCTION, 1L);
        session.setAttribute(ANNOUNCED_FEATURES_V1_1_0, 1L);

        ObaArrivalInfo[] obaArrivalInfoArray = new ObaArrivalInfo[0];
        new Expectations() {{
            obaArrivalInfoResponse.getArrivalInfo(); result = obaArrivalInfoArray;
            obaUserClient.getArrivalsAndDeparturesForStop(anyString, anyInt); result = obaArrivalInfoResponse;
        }};

        SpeechletResponse sr = authedSpeechlet.onLaunch(
                launchRequest,
                session);
        String spoken = ((PlainTextOutputSpeech)sr.getOutputSpeech()).getText();
        assertThat(spoken, equalTo("There are no upcoming arrivals at your stop for the next "
                + ARRIVALS_SCAN_MINS + " minutes"));
    }

    @Test
    public void setStopWithDuplicateIdsYesFirst() throws SpeechletException, IOException {
        // Turn off tutorials
        session.setAttribute(ANNOUNCED_INTRODUCTION, 1L);
        session.setAttribute(ANNOUNCED_FEATURES_V1_1_0, 1L);

        String newStopCode = "2340";
        String stopName1 = "ABC";
        String stopName2 = "123";
        setupStopWithDuplicateIds(newStopCode, stopName1, stopName2);

        // Now say the Yes intent
        SpeechletResponse sr = authedSpeechlet.onIntent(
                IntentRequest.builder()
                        .withRequestId("test-request-id2")
                        .withIntent(
                                Intent.builder()
                                        .withName(YES)
                                        .build()
                        )
                        .build(),
                session
        );
        String spoken = ((PlainTextOutputSpeech) sr.getOutputSpeech()).getText();
        assertThat(spoken, startsWith("Ok, your stop number is 2340 in the Puget Sound region."));
    }

    @Test
    public void setStopWithDuplicateIdsNoThenYes() throws SpeechletException, IOException {
        // Turn off tutorials
        session.setAttribute(ANNOUNCED_INTRODUCTION, 1L);
        session.setAttribute(ANNOUNCED_FEATURES_V1_1_0, 1L);

        String newStopCode = "2340";
        String stopName1 = "ABC";
        String stopName2 = "123";
        setupStopWithDuplicateIds(newStopCode, stopName1, stopName2);

        // Now say the No intent
        SpeechletResponse sr = authedSpeechlet.onIntent(
                IntentRequest.builder()
                        .withRequestId("test-request-id2")
                        .withIntent(
                                Intent.builder()
                                        .withName(NO)
                                        .build()
                        )
                        .build(),
                session
        );
        String spoken = ((PlainTextOutputSpeech) sr.getOutputSpeech()).getText();
        assertThat(spoken, startsWith("Ok, what about the 123 stop?"));

        // Alexa does some data conversion for session variables - we need to imitate that here for obaStopsArray
        ArrayList<LinkedHashMap<String, String>> list = new ArrayList<>();
        LinkedHashMap<String, String> obaStop2Serialized = new LinkedHashMap<>();
        obaStop2Serialized.put("id", newStopCode);
        obaStop2Serialized.put("stopCode", newStopCode);
        obaStop2Serialized.put("name", stopName2);
        list.add(obaStop2Serialized);
        session.setAttribute(DIALOG_FOUND_STOPS, list);

        // Now say the YES intent
        sr = authedSpeechlet.onIntent(
                IntentRequest.builder()
                        .withRequestId("test-request-id2")
                        .withIntent(
                                Intent.builder()
                                        .withName(YES)
                                        .build()
                        )
                        .build(),
                session
        );
        spoken = ((PlainTextOutputSpeech) sr.getOutputSpeech()).getText();
        assertThat(spoken, startsWith("Ok, your stop number is 2340 in the Puget Sound region."));
    }

    @Test
    public void setStopWithDuplicateIdsNoThenNo() throws SpeechletException, IOException {
        String newStopCode = "2340";
        String stopName1 = "ABC";
        String stopName2 = "123";
        setupStopWithDuplicateIds(newStopCode, stopName1, stopName2);

        // Now say the No intent
        SpeechletResponse sr = authedSpeechlet.onIntent(
                IntentRequest.builder()
                        .withRequestId("test-request-id2")
                        .withIntent(
                                Intent.builder()
                                        .withName(NO)
                                        .build()
                        )
                        .build(),
                session
        );
        String spoken = ((PlainTextOutputSpeech) sr.getOutputSpeech()).getText();
        assertThat(spoken, startsWith("Ok, what about the 123 stop?"));

        // Alexa does some data conversion for session variables - we need to imitate that here for obaStopsArray
        ArrayList<LinkedHashMap<String, String>> list = new ArrayList<>();
        LinkedHashMap<String, String> obaStop2Serialized = new LinkedHashMap<>();
        obaStop2Serialized.put("id", newStopCode);
        obaStop2Serialized.put("stopCode", newStopCode);
        obaStop2Serialized.put("name", stopName2);
        list.add(obaStop2Serialized);
        session.setAttribute(DIALOG_FOUND_STOPS, list);

        // Now say the NO intent
        sr = authedSpeechlet.onIntent(
                IntentRequest.builder()
                        .withRequestId("test-request-id2")
                        .withIntent(
                                Intent.builder()
                                        .withName(NO)
                                        .build()
                        )
                        .build(),
                session
        );
        spoken = ((PlainTextOutputSpeech) sr.getOutputSpeech()).getText();
        assertThat(spoken, startsWith("OneBusAway could not locate your stop number."));
    }

    private void setupStopWithDuplicateIds(String stopCode, String stopName1, String stopName2) throws SpeechletException, IOException {
        // Mock persisted user data for Test Region 2
        testUserData.setUserId(TEST_USER_ID);
        testUserData.setStopId("6497");
        testUserData.setCity(TEST_REGION_2.getName());
        testUserData.setRegionName(TEST_REGION_2.getName());
        testUserData.setRegionId(TEST_REGION_2.getId());
        testUserData.setObaBaseUrl(TEST_REGION_2.getObaBaseUrl());


        // Mock stop info
        ObaStop[] obaStopsArray = new ObaStop[2];
        obaStopsArray[0] = obaStop;
        obaStopsArray[1] = obaStop2;

        new NonStrictExpectations() {{
            googleMaps.geocode(TEST_REGION_2.getName());
            Location l = new Location("test");
            l.setLatitude(27.9681);
            l.setLongitude(-82.4764);
            result = Optional.of(l);

            obaStop.getStopCode();
            result = stopCode;
            obaStop.getId();
            result = stopCode;
            obaStop2.getStopCode();
            result = stopCode;
            obaStop2.getId();
            result = stopCode;
            obaUserClient.getStopFromCode(l, stopCode);
            result = obaStopsArray;

            obaStop.getName();
            result = stopName1;
            obaStop2.getName();
            result = stopName2;
            obaClient.getClosestRegion(l, false);
            result = Optional.of(TEST_REGION_2);
        }};

        HashMap<String, Slot> slots = new HashMap<>();
        slots.put(STOP_ID, Slot.builder()
                .withName(STOP_ID)
                .withValue(stopCode).build());
        SpeechletResponse sr = authedSpeechlet.onIntent(
                IntentRequest.builder()
                        .withRequestId("test-request-id")
                        .withIntent(
                                Intent.builder()
                                        .withName(SET_STOP_NUMBER)
                                        .withSlots(slots)
                                        .build()
                        )
                        .build(),
                session
        );
        String spoken = ((PlainTextOutputSpeech)sr.getOutputSpeech()).getText();
        assertEquals(spoken, "We found 2 stops associated with the stop number. Did you mean the ABC stop?");

        // Alexa does some data conversion for session variables - we need to imitate that here for obaStopsArray
        ArrayList<LinkedHashMap<String, String>> list = new ArrayList<>();
        LinkedHashMap<String, String> obaStop2Serialized = new LinkedHashMap<>();
        LinkedHashMap<String, String> obaStopSerialized = new LinkedHashMap<>();
        obaStopSerialized.put("id", stopCode);
        obaStopSerialized.put("stopCode", stopCode);
        obaStopSerialized.put("name", stopName1);
        obaStop2Serialized.put("id", stopCode);
        obaStop2Serialized.put("stopCode", stopCode);
        obaStop2Serialized.put("name", stopName2);
        list.add(obaStopSerialized);
        list.add(obaStop2Serialized);
        session.setAttribute(DIALOG_FOUND_STOPS, list);
    }

    @Test
    public void setRouteFilterNoYes() throws SpeechletException, IOException {
        String stopId = "6497";
        String stopCode = stopId;

        // Route 1
        String routeId = "Hillsborough Area Regional Transit_1";
        String routeShortName = "1";
        String routeLongName = "40th Street";

        // Route 2
        String routeId2 = "Hillsborough Area Regional Transit_2";
        String routeShortName2 = "2";
        String routeLongName2 = "Nebraska Avenue";

        setupRouteFilter(stopId, routeId, routeShortName, routeLongName, routeId2, routeShortName2, routeLongName2);

        // Ask to set the route filter
        SpeechletResponse sr = authedSpeechlet.onIntent(
                IntentRequest.builder()
                        .withRequestId("test-request-id2")
                        .withIntent(
                                Intent.builder()
                                        .withName(SET_ROUTE_FILTER)
                                        .build()
                        )
                        .build(),
                session
        );
        String spoken = ((PlainTextOutputSpeech) sr.getOutputSpeech()).getText();
        assertThat(spoken, startsWith("Sure, let's set up a route filter for stop " + stopCode + ".  Do you want to hear arrivals for Route " + routeShortName + "?"));

        // Alexa does some data conversion for session variables - we need to imitate that here for routes
        ArrayList<LinkedHashMap<String, String>> list = new ArrayList<>();
        LinkedHashMap<String, String> route1Serialized = new LinkedHashMap<>();
        route1Serialized.put("id", routeId);
        route1Serialized.put("shortName", routeShortName);
        route1Serialized.put("longName", routeLongName);
        list.add(route1Serialized);
        LinkedHashMap<String, String> route2Serialized = new LinkedHashMap<>();
        route2Serialized.put("id", routeId2);
        route2Serialized.put("shortName", routeShortName2);
        route2Serialized.put("longName", routeLongName2);
        list.add(route2Serialized);
        session.setAttribute(DIALOG_ROUTES_TO_ASK_ABOUT, list);

        // First say the Yes intent
        sr = authedSpeechlet.onIntent(
                IntentRequest.builder()
                        .withRequestId("test-request-id2")
                        .withIntent(
                                Intent.builder()
                                        .withName(YES)
                                        .build()
                        )
                        .build(),
                session
        );

        spoken = ((PlainTextOutputSpeech) sr.getOutputSpeech()).getText();
        assertThat(spoken, startsWith("Ok, how about Route " + routeShortName2 + "?"));

        // Then say YES again
        sr = authedSpeechlet.onIntent(
                IntentRequest.builder()
                        .withRequestId("test-request-id2")
                        .withIntent(
                                Intent.builder()
                                        .withName(YES)
                                        .build()
                        )
                        .build(),
                session
        );

        spoken = ((PlainTextOutputSpeech) sr.getOutputSpeech()).getText();
        assertThat(spoken, startsWith("Alright, I've saved your route filter for stop " + stopCode + "."));

        // Test to make sure that both routes are being filtered out
        HashMap<String, HashSet<String>> routeFilters = testUserData.getRoutesToFilterOut();
        HashSet<String> routesToFilterHashSet = routeFilters.get(stopId);
        assertFalse(routesToFilterHashSet.contains(routeId));
        assertFalse(routesToFilterHashSet.contains(routeId2));
    }

    @Test
    public void setRouteFilterYesNo() throws SpeechletException, IOException {
        String stopId = "6497";
        String stopCode = stopId;

        // Route 1
        String routeId = "Hillsborough Area Regional Transit_1";
        String routeShortName = "1";
        String routeLongName = "40th Street";

        // Route 2
        String routeId2 = "Hillsborough Area Regional Transit_2";
        String routeShortName2 = "2";
        String routeLongName2 = "Nebraska Avenue";

        setupRouteFilter(stopId, routeId, routeShortName, routeLongName, routeId2, routeShortName2, routeLongName2);

        // Ask to set the route filter
        SpeechletResponse sr = authedSpeechlet.onIntent(
                IntentRequest.builder()
                        .withRequestId("test-request-id2")
                        .withIntent(
                                Intent.builder()
                                        .withName(SET_ROUTE_FILTER)
                                        .build()
                        )
                        .build(),
                session
        );
        String spoken = ((PlainTextOutputSpeech) sr.getOutputSpeech()).getText();
        assertThat(spoken, startsWith("Sure, let's set up a route filter for stop " + stopCode + ".  Do you want to hear arrivals for Route " + routeShortName + "?"));


        // Alexa does some data conversion for session variables - we need to imitate that here for routes
        ArrayList<LinkedHashMap<String, String>> list = new ArrayList<>();
        LinkedHashMap<String, String> route1Serialized = new LinkedHashMap<>();
        route1Serialized.put("id", routeId);
        route1Serialized.put("shortName", routeShortName);
        route1Serialized.put("longName", routeLongName);
        list.add(route1Serialized);
        LinkedHashMap<String, String> route2Serialized = new LinkedHashMap<>();
        route2Serialized.put("id", routeId2);
        route2Serialized.put("shortName", routeShortName2);
        route2Serialized.put("longName", routeLongName2);
        list.add(route2Serialized);
        session.setAttribute(DIALOG_ROUTES_TO_ASK_ABOUT, list);

        // Say YES
        sr = authedSpeechlet.onIntent(
                IntentRequest.builder()
                        .withRequestId("test-request-id2")
                        .withIntent(
                                Intent.builder()
                                        .withName(YES)
                                        .build()
                        )
                        .build(),
                session
        );

        spoken = ((PlainTextOutputSpeech) sr.getOutputSpeech()).getText();
        assertThat(spoken, startsWith("Ok, how about Route " + routeShortName2 + "?"));

        // Say NO
        sr = authedSpeechlet.onIntent(
                IntentRequest.builder()
                        .withRequestId("test-request-id2")
                        .withIntent(
                                Intent.builder()
                                        .withName(NO)
                                        .build()
                        )
                        .build(),
                session
        );

        spoken = ((PlainTextOutputSpeech) sr.getOutputSpeech()).getText();
        assertThat(spoken, startsWith("Alright, I've saved your route filter for stop " + stopCode + "."));

        // Test to make sure that both routes are being filtered out
        HashMap<String, HashSet<String>> routeFilters = testUserData.getRoutesToFilterOut();
        HashSet<String> routesToFilterHashSet = routeFilters.get(stopId);
        assertFalse(routesToFilterHashSet.contains(routeId));
        assertTrue(routesToFilterHashSet.contains(routeId2));
    }

    @Test
    public void setRouteFilterYesYes() throws SpeechletException, IOException {
        String stopId = "6497";
        String stopCode = stopId;

        // Route 1
        String routeId = "Hillsborough Area Regional Transit_1";
        String routeShortName = "1";
        String routeLongName = "40th Street";

        // Route 2
        String routeId2 = "Hillsborough Area Regional Transit_2";
        String routeShortName2 = "2";
        String routeLongName2 = "Nebraska Avenue";

        setupRouteFilter(stopId, routeId, routeShortName, routeLongName, routeId2, routeShortName2, routeLongName2);

        // Ask to set the route filter
        SpeechletResponse sr = authedSpeechlet.onIntent(
                IntentRequest.builder()
                        .withRequestId("test-request-id2")
                        .withIntent(
                                Intent.builder()
                                        .withName(SET_ROUTE_FILTER)
                                        .build()
                        )
                        .build(),
                session
        );
        String spoken = ((PlainTextOutputSpeech) sr.getOutputSpeech()).getText();
        assertThat(spoken, startsWith("Sure, let's set up a route filter for stop " + stopCode + ".  Do you want to hear arrivals for Route " + routeShortName + "?"));

        // Alexa does some data conversion for session variables - we need to imitate that here for routes
        ArrayList<LinkedHashMap<String, String>> list = new ArrayList<>();
        LinkedHashMap<String, String> route1Serialized = new LinkedHashMap<>();
        route1Serialized.put("id", routeId);
        route1Serialized.put("shortName", routeShortName);
        route1Serialized.put("longName", routeLongName);
        list.add(route1Serialized);
        LinkedHashMap<String, String> route2Serialized = new LinkedHashMap<>();
        route2Serialized.put("id", routeId2);
        route2Serialized.put("shortName", routeShortName2);
        route2Serialized.put("longName", routeLongName2);
        list.add(route2Serialized);
        session.setAttribute(DIALOG_ROUTES_TO_ASK_ABOUT, list);

        // First say the No intent
        sr = authedSpeechlet.onIntent(
                IntentRequest.builder()
                        .withRequestId("test-request-id2")
                        .withIntent(
                                Intent.builder()
                                        .withName(NO)
                                        .build()
                        )
                        .build(),
                session
        );

        spoken = ((PlainTextOutputSpeech) sr.getOutputSpeech()).getText();
        assertThat(spoken, startsWith("Ok, how about Route " + routeShortName2 + "?"));

        // Say YES
        sr = authedSpeechlet.onIntent(
                IntentRequest.builder()
                        .withRequestId("test-request-id2")
                        .withIntent(
                                Intent.builder()
                                        .withName(YES)
                                        .build()
                        )
                        .build(),
                session
        );

        spoken = ((PlainTextOutputSpeech) sr.getOutputSpeech()).getText();
        assertThat(spoken, startsWith("Alright, I've saved your route filter for stop " + stopCode + "."));

        // Test to make sure that both routes are being filtered out
        HashMap<String, HashSet<String>> routeFilters = testUserData.getRoutesToFilterOut();
        HashSet<String> routesToFilterHashSet = routeFilters.get(stopId);
        assertTrue(routesToFilterHashSet.contains(routeId));
        assertFalse(routesToFilterHashSet.contains(routeId2));
    }

    @Test
    public void setRouteFilterNoNo() throws SpeechletException, IOException {
        String stopId = "6497";
        String stopCode = stopId;

        // Route 1
        String routeId = "Hillsborough Area Regional Transit_1";
        String routeShortName = "1";
        String routeLongName = "40th Street";

        // Route 2
        String routeId2 = "Hillsborough Area Regional Transit_2";
        String routeShortName2 = "2";
        String routeLongName2 = "Nebraska Avenue";

        setupRouteFilter(stopId, routeId, routeShortName, routeLongName, routeId2, routeShortName2, routeLongName2);

        // Ask to set the route filter
        SpeechletResponse sr = authedSpeechlet.onIntent(
                IntentRequest.builder()
                        .withRequestId("test-request-id2")
                        .withIntent(
                                Intent.builder()
                                        .withName(SET_ROUTE_FILTER)
                                        .build()
                        )
                        .build(),
                session
        );
        String spoken = ((PlainTextOutputSpeech) sr.getOutputSpeech()).getText();
        assertThat(spoken, startsWith("Sure, let's set up a route filter for stop " + stopCode + ".  Do you want to hear arrivals for Route " + routeShortName + "?"));


        // Alexa does some data conversion for session variables - we need to imitate that here for routes
        ArrayList<LinkedHashMap<String, String>> list = new ArrayList<>();
        LinkedHashMap<String, String> route1Serialized = new LinkedHashMap<>();
        route1Serialized.put("id", routeId);
        route1Serialized.put("shortName", routeShortName);
        route1Serialized.put("longName", routeLongName);
        list.add(route1Serialized);
        LinkedHashMap<String, String> route2Serialized = new LinkedHashMap<>();
        route2Serialized.put("id", routeId2);
        route2Serialized.put("shortName", routeShortName2);
        route2Serialized.put("longName", routeLongName2);
        list.add(route2Serialized);
        session.setAttribute(DIALOG_ROUTES_TO_ASK_ABOUT, list);

        // Say No
        sr = authedSpeechlet.onIntent(
                IntentRequest.builder()
                        .withRequestId("test-request-id2")
                        .withIntent(
                                Intent.builder()
                                        .withName(NO)
                                        .build()
                        )
                        .build(),
                session
        );

        spoken = ((PlainTextOutputSpeech) sr.getOutputSpeech()).getText();
        assertThat(spoken, startsWith("Ok, how about Route " + routeShortName2 + "?"));

        // Say No
        sr = authedSpeechlet.onIntent(
                IntentRequest.builder()
                        .withRequestId("test-request-id2")
                        .withIntent(
                                Intent.builder()
                                        .withName(NO)
                                        .build()
                        )
                        .build(),
                session
        );

        spoken = ((PlainTextOutputSpeech) sr.getOutputSpeech()).getText();
        assertThat(spoken, startsWith("Alright, I've saved your route filter for stop " + stopCode + "."));

        // Test to make sure that both routes are being filtered out
        HashMap<String, HashSet<String>> routeFilters = testUserData.getRoutesToFilterOut();
        HashSet<String> routesToFilterHashSet = routeFilters.get(stopId);
        assertTrue(routesToFilterHashSet.contains(routeId));
        assertTrue(routesToFilterHashSet.contains(routeId2));
    }

    @Test
    public void setRouteFilterOnlyOneRoute() throws SpeechletException, IOException {
        String stopId = "6497";
        String stopCode = stopId;

        // Route 1
        String routeId = "Hillsborough Area Regional Transit_1";
        String routeShortName = "1";
        String routeLongName = "40th Street";

        // Route 2 - set to null so it's not added to route list (we only want one route in list)
        setupRouteFilter(stopId, routeId, routeShortName, routeLongName, null, null, null);

        // Ask to set the route filter
        SpeechletResponse sr = authedSpeechlet.onIntent(
                IntentRequest.builder()
                        .withRequestId("test-request-id2")
                        .withIntent(
                                Intent.builder()
                                        .withName(SET_ROUTE_FILTER)
                                        .build()
                        )
                        .build(),
                session
        );
        String spoken = ((PlainTextOutputSpeech) sr.getOutputSpeech()).getText();
        assertThat(spoken, startsWith("There is only one route for stop " + stopCode + ", so I can't filter out any routes."));
    }

    /**
     * Properites to set up a route filter with.  If routeId2 properties are null, then only one route is added for the
     * stop, otherwise 2 routes are added for the stop.
     *
     * @param stopId
     * @param routeId
     * @param routeShortName
     * @param routeLongName
     * @param routeId2
     * @param routeShortName2
     * @param routeLongName2
     * @throws IOException
     */
    private void setupRouteFilter(String stopId, String routeId, String routeShortName, String routeLongName, String routeId2, String routeShortName2, String routeLongName2) throws IOException {
        // Mock persisted user data
        testUserData.setUserId(TEST_USER_ID);
        testUserData.setStopId(stopId);
        testUserData.setCity(TEST_REGION_1.getName());
        testUserData.setRegionName(TEST_REGION_1.getName());
        testUserData.setRegionId(TEST_REGION_1.getId());
        testUserData.setObaBaseUrl(TEST_REGION_1.getObaBaseUrl());

        ObaRoute[] obaRouteArray;

        // Mock route info
        if (routeId2 != null) {
            obaRouteArray = new ObaRoute[2];
            obaRouteArray[0] = obaRoute;
            obaRouteArray[1] = obaRoute2;
        } else {
            obaRouteArray = new ObaRoute[1];
            obaRouteArray[0] = obaRoute;
        }

        new NonStrictExpectations() {{
            obaRoute.getId();
            result = routeId;
            obaRoute.getShortName();
            result = routeShortName;
            obaRoute.getLongName();
            result = routeLongName;

            obaRoute2.getId();
            result = routeId2;
            obaRoute2.getShortName();
            result = routeShortName2;
            obaRoute2.getLongName();
            result = routeLongName2;

            obaStopResponse.getStopCode();
            result = stopId;
            obaStopResponse.getRoutes();
            result = obaRouteArray;
            obaUserClient.getStop(anyString);
            result = obaStopResponse;
        }};
    }

    @Test
    public void enableExperimentalRegions() throws SpeechletException, IOException {
        obaRegions = new ArrayList<>(4);
        obaRegions.add(TEST_REGION_1);
        obaRegions.add(TEST_REGION_2);
        obaRegions.add(TEST_REGION_3);
        obaRegions.add(TEST_REGION_EXPERIMENTAL);

        new Expectations() {{
            obaUserClient.getAllRegions(anyBoolean);
            result = obaRegions;
        }};

        SpeechletResponse sr = authedSpeechlet.onIntent(
                IntentRequest.builder()
                        .withRequestId("test-request-id")
                        .withIntent(
                                Intent.builder()
                                        .withName(ENABLE_EXPERIMENTAL_REGIONS)
                                        .withSlots(new HashMap<>())
                                        .build()
                        )
                        .build(),
                session
        );
        String spoken = ((PlainTextOutputSpeech) sr.getOutputSpeech()).getText();
        assertThat(spoken, containsString("Experimental regions are now enabled"));
        assertThat(spoken, containsString(TEST_REGION_EXPERIMENTAL.getName().replace(" (beta)", "")));
        assertEquals(session.getAttribute(EXPERIMENTAL_REGIONS), true);
        assertEquals(testUserData.isExperimentalRegions(), true);
    }

    @Test
    public void disableExperimentalRegions() throws SpeechletException, IOException {
        obaRegions = new ArrayList<>(4);
        obaRegions.add(TEST_REGION_1);
        obaRegions.add(TEST_REGION_2);
        obaRegions.add(TEST_REGION_3);
        obaRegions.add(TEST_REGION_EXPERIMENTAL);

        new Expectations() {{
            obaUserClient.getAllRegions(anyBoolean);
            result = obaRegions;
        }};

        SpeechletResponse sr = authedSpeechlet.onIntent(
                IntentRequest.builder()
                        .withRequestId("test-request-id")
                        .withIntent(
                                Intent.builder()
                                        .withName(DISABLE_EXPERIMENTAL_REGIONS)
                                        .withSlots(new HashMap<>())
                                        .build()
                        )
                        .build(),
                session
        );
        String spoken = ((PlainTextOutputSpeech) sr.getOutputSpeech()).getText();
        assertThat(spoken, containsString("Experimental regions are now disabled"));
        assertFalse(spoken.contains(TEST_REGION_EXPERIMENTAL.getName()));
        assertEquals(session.getAttribute(EXPERIMENTAL_REGIONS), false);
        assertEquals(testUserData.isExperimentalRegions(), false);
    }

    @Test
    public void goodbye() throws SpeechletException, IOException {
        TestUtil.assertGoodbye(authedSpeechlet, session);
    }

    public void allIntents() throws SpeechletException, IOException, IllegalAccessException {
        new Expectations() {{
            obaClient.getAllRegions(false);
            ArrayList<ObaRegion> regions = new ArrayList<>(1);
            regions.add(TEST_REGION_1);
            regions.add(TEST_REGION_2);
            regions.add(TEST_REGION_1);
            result = regions;
        }};

        TestUtil.assertAllIntents(authedSpeechlet, session);
    }
}
