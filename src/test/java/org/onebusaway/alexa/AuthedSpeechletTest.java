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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onebusaway.alexa.lib.GoogleMaps;
import org.onebusaway.alexa.lib.ObaClient;
import org.onebusaway.alexa.lib.ObaUserClient;
import org.onebusaway.alexa.storage.ObaDao;
import org.onebusaway.alexa.storage.ObaUserDataItem;
import org.onebusaway.io.client.elements.ObaArrivalInfo;
import org.onebusaway.io.client.elements.ObaRegion;
import org.onebusaway.io.client.elements.ObaRegionElement;
import org.onebusaway.io.client.elements.ObaStop;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.onebusaway.alexa.ObaIntent.*;
import static org.onebusaway.alexa.SessionAttribute.CITY_NAME;
import static org.onebusaway.alexa.SessionAttribute.STOP_NUMBER;

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

    @Mocked
    ObaArrivalInfo obaArrivalInfo;

    @Mocked
    ObaStop obaStop;

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
                                        .withName("GetStopNumberIntent")
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
        assertThat(spoken, equalTo("Route 8 Mlk Way Jr is now arriving based on the schedule -- "));
    }

    @Test
    public void setStopNumber() throws SpeechletException, IOException {
        String newStopCode = "3105";

        // Mock persisted user data
        testUserData.setUserId(TEST_USER_ID);
        testUserData.setStopId("6497");
        testUserData.setCity(TEST_REGION_1.getName());
        testUserData.setRegionName(TEST_REGION_1.getName());
        testUserData.setRegionId(TEST_REGION_1.getId());
        testUserData.setObaBaseUrl(TEST_REGION_1.getObaBaseUrl());

        // Mock stop info
        ObaStop[] obaStopsArray = new ObaStop[1];
        obaStopsArray[0] = obaStop;

        new Expectations() {{
            googleMaps.geocode(TEST_REGION_1.getName());
            Location l = new Location("test");
            l.setLatitude(27.9681);
            l.setLongitude(-82.4764);
            result = Optional.of(l);

            obaStop.getStopCode(); result = newStopCode;
            obaStop.getId(); result = newStopCode;
            obaUserClient.getStopFromCode(l, newStopCode); result = obaStopsArray;

            obaClient.getClosestRegion(l); result = Optional.of(TEST_REGION_1);

            obaDao.getUserData(session); result = Optional.of(testUserData);
        }};

        HashMap<String, Slot> slots = new HashMap<>();
        slots.put(STOP_NUMBER, Slot.builder()
                .withName(STOP_NUMBER)
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
        String spoken = ((PlainTextOutputSpeech)sr.getOutputSpeech()).getText();
        assertThat(spoken, startsWith("Ok, your stop number is " + newStopCode + " in the " + TEST_REGION_1.getName() + " region. " +
                "Great.  I am ready to tell you about the next bus."));
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

            obaClient.getClosestRegion(l); result = Optional.of(TEST_REGION_1);
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
    public void help() throws SpeechletException {
        SpeechletResponse sr = authedSpeechlet.onIntent(
                IntentRequest.builder()
                        .withRequestId("test-request-id")
                        .withIntent(
                                Intent.builder()
                                        .withName("AMAZON.HelpIntent")
                                        .withSlots(new HashMap<String, Slot>())
                                        .build()
                        )
                        .build(),
                session
        );
        String spoken = ((PlainTextOutputSpeech)sr.getOutputSpeech()).getText();
        assertThat(spoken, containsString("You've already configured your region and stop"));
    }

    @Test
    public void repeat() throws SpeechletException, URISyntaxException, IOException {
        IntentRequest repeatIntent = IntentRequest.builder()
                .withRequestId("test-request-id")
                .withIntent(
                        Intent.builder()
                                .withName("AMAZON.RepeatIntent")
                                .withSlots(new HashMap<String, Slot>())
                                .build()
                )
                .build();
        // Try the repeat intent - this simulates after initial setup, when we don't yet have anything to repeat
        SpeechletResponse sr = authedSpeechlet.onIntent(repeatIntent, session);
        String spoken = ((PlainTextOutputSpeech)sr.getOutputSpeech()).getText();
        assertThat(spoken, containsString("I'm sorry, I don't have anything to repeat.  You can ask me for arrival times for your stop."));

        ObaArrivalInfo[] obaArrivalInfoArray = new ObaArrivalInfo[1];
        obaArrivalInfoArray[0] = obaArrivalInfo;
        new Expectations() {{
            obaArrivalInfo.getShortName(); result = "8";
            obaArrivalInfo.getHeadsign(); result = "Mlk Way Jr";
            obaArrivalInfoResponse.getArrivalInfo(); result = obaArrivalInfoArray;
            obaUserClient.getArrivalsAndDeparturesForStop(anyString, anyInt); result = obaArrivalInfoResponse;
        }};
        String response = "Route 8 Mlk Way Jr is now arriving based on the schedule -- ";

        // Test initial request/response - this should also save the response for later retrieval via repeat intent
        sr = authedSpeechlet.onLaunch(launchRequest, session);
        spoken = ((PlainTextOutputSpeech)sr.getOutputSpeech()).getText();
        assertThat(spoken, equalTo(response));

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
                + AuthedSpeechlet.ARRIVALS_SCAN_MINS + " minutes."));
    }

    @Test
    public void goodbye() throws SpeechletException, IOException {
        SpeechletResponse sr = authedSpeechlet.onIntent(
                IntentRequest.builder()
                        .withRequestId("test-request-id")
                        .withIntent(
                                Intent.builder()
                                        .withName(STOP)
                                        .withSlots(new HashMap<String, Slot>())
                                        .build()
                        )
                        .build(),
                session
        );
        String spoken = ((PlainTextOutputSpeech)sr.getOutputSpeech()).getText();
        assertThat(spoken, containsString("Good-bye"));
    }

    public void allIntents() throws SpeechletException, IOException, IllegalAccessException {
        new Expectations() {{
            obaClient.getAllRegions();
            ArrayList<ObaRegion> regions = new ArrayList<>(1);
            regions.add(TEST_REGION_1);
            regions.add(TEST_REGION_2);
            regions.add(TEST_REGION_1);
            result = regions;
        }};

        TestUtil.assertAllIntents(authedSpeechlet, session);
    }
}
