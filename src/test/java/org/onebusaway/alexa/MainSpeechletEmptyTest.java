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
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import org.junit.Before;
import org.junit.BeforeClass;
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
import org.onebusaway.location.Location;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import util.TestUtil;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.onebusaway.alexa.ObaIntent.*;
import static org.onebusaway.alexa.SessionAttribute.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class,
                      classes = UnitTests.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class MainSpeechletEmptyTest {
    static final LaunchRequest launchRequest = LaunchRequest.builder().withRequestId("test-req-id").build();
    static final String TEST_USER_ID = "test-user-id";
    static final User testUser = User.builder().withUserId(TEST_USER_ID).build();

    private static final ObaRegion TEST_REGION_1 = new ObaRegionElement(
            1,
            "Test Region 1",
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
    private static final ObaRegion TEST_REGION_2 = new ObaRegionElement(
            1,
            "Test Region 2",
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

    @BeforeClass
    public static void setUpMocksForSpringConfiguration() {
        new MockUp<ObaDao>() {
            @Mock
            Optional<ObaUserDataItem> getUserData(Session s) {
                return Optional.empty();
            }
        };
    }

    @Mocked
    GoogleMaps googleMaps;

    @Mocked
    ObaDao obaDao;

    @Mocked
    ObaClient obaClient;

    @Mocked
    ObaUserClient obaUserClient;

    @Mocked
    ObaArrivalInfo obaArrivalInfo;

    @Mocked
    ObaArrivalInfoResponse obaArrivalInfoResponse;

    @Mocked
    ObaStop obaStop;

    Session session;

    @Resource
    MainSpeechlet mainSpeechlet;

    @Before
    public void resetFactoryObjects() {
        session = Session.builder().withUser(testUser).withSessionId("test-session-id").build();
    }

    @Test
    public void launchAsksForCity() throws SpeechletException {
        SpeechletResponse sr = mainSpeechlet.onLaunch(
                launchRequest,
                session);

        assertThat(sr.getOutputSpeech(), is(instanceOf(PlainTextOutputSpeech.class)));
        String spoken = ((PlainTextOutputSpeech)sr.getOutputSpeech()).getText();
        assertWelcomeResponse(spoken);
    }

    private void assertWelcomeResponse(String spoken) {
        assertThat(spoken, containsString("Welcome to "));
        assertThat(spoken, containsString("In what city do you live?"));
    }

    @Test
    public void help() throws SpeechletException {
        SpeechletResponse sr = mainSpeechlet.onIntent(
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
        assertWelcomeResponse(spoken);
    }

    @Test
    public void getCity() throws SpeechletException {
        SpeechletResponse sr = mainSpeechlet.onIntent(
                IntentRequest.builder()
                .withRequestId("test-request-id")
                .withIntent(
                        Intent.builder()
                        .withName(GET_CITY)
                        .withSlots(new HashMap<String, Slot>())
                        .build()
                )
                .build(),
                session
        );
        assertThat(sr.getOutputSpeech(), is(instanceOf(PlainTextOutputSpeech.class)));
        String spoken = ((PlainTextOutputSpeech)sr.getOutputSpeech()).getText();
        assertThat(spoken, containsString("You have not yet told me where you live."));
    }

    @Test
    public void setRecognizableCitySeattle() throws SpeechletException, IOException {
        new Expectations() {{
            googleMaps.geocode("Seattle");
            Location l = new Location("test");
            l.setLatitude(47.6097);
            l.setLongitude(-122.3331);
            result = Optional.of(l);

            obaClient.getClosestRegion(l);
            result = Optional.of(TEST_REGION_1);
        }};
        HashMap<String, Slot> slots = new HashMap<>();
        slots.put(CITY_NAME, Slot.builder()
                                  .withName(CITY_NAME)
                                  .withValue("Seattle").build());
        SpeechletResponse sr = mainSpeechlet.onIntent(
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
    public void unrecognizedCity() throws SpeechletException, IOException {
        new Expectations() {{
            obaClient.getAllRegions();
            ArrayList<ObaRegion> regions = new ArrayList<>(1);
            regions.add(TEST_REGION_1);
            regions.add(TEST_REGION_2);
            regions.add(TEST_REGION_1);
            result = regions;
        }};
        HashMap<String, Slot> slots = new HashMap<>();
        slots.put(CITY_NAME, Slot.builder()
                .withName(CITY_NAME)
                .withValue("Houston").build());
        SpeechletResponse sr = mainSpeechlet.onIntent(
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
        assertThat(spoken, containsString("OneBusAway could not locate a OneBusAway " +
                "region near Houston, the city you gave. " +
                "Supported regions include " + TEST_REGION_1.getName() + ", " +
                TEST_REGION_1.getName() + ", and " + TEST_REGION_2.getName() + ". " +
                "Tell me again, what's the largest city near you?"));
        assertThat(session.getAttribute(ASK_STATE), equalTo(AskState.NONE.toString()));
    }

    @Test
    public void setCityWithoutCityName() throws SpeechletException {
        HashMap<String, Slot> slots = new HashMap<>();
        slots.put(CITY_NAME, Slot.builder()
                .withName(CITY_NAME)
                .withValue(null).build());
        SpeechletResponse sr = mainSpeechlet.onIntent(
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
        assertThat(spoken, containsString("In what city do you live?"));
    }

    @Test
    public void setStopWithoutStopNumber() throws SpeechletException {
        HashMap<String, Slot> slots = new HashMap<>();
        slots.put(STOP_ID, Slot.builder()
                .withName(STOP_ID)
                .withValue(null).build());
        SpeechletResponse sr = mainSpeechlet.onIntent(
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
        assertThat(spoken, containsString("What is your stop number?"));
    }

    @Test
    public void setStopBeforeCity() throws SpeechletException, IOException {
        String newStopCode = "2245";
        ObaStop[] obaStopsArray = new ObaStop[1];
        obaStopsArray[0] = obaStop;

        new Expectations() {{
            googleMaps.geocode("Seattle");
            Location l = new Location("test");
            l.setLatitude(47.6097);
            l.setLongitude(-122.3331);
            result = Optional.of(l);

            obaClient.getClosestRegion(l);
            result = Optional.of(TEST_REGION_1);

            obaStop.getStopCode();
            result = newStopCode;
            obaStop.getId();
            result = newStopCode;
            obaUserClient.getStopFromCode(l, newStopCode);
            result = obaStopsArray;
        }};

        HashMap<String, Slot> slots = new HashMap<>();
        slots.put(STOP_ID, Slot.builder()
                .withName(STOP_ID)
                .withValue(newStopCode).build());
        SpeechletResponse sr = mainSpeechlet.onIntent(
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
        assertThat(spoken, equalTo("You haven't set your region yet. In what city is stop " + newStopCode + "?"));
        assertThat(session.getAttribute(ASK_STATE), equalTo(AskState.STOP_BEFORE_CITY.toString()));
        assertThat(session.getAttribute(STOP_ID), equalTo(newStopCode));

        HashMap<String, Slot> citySlots = new HashMap<>();
        citySlots.put(CITY_NAME, Slot.builder()
                .withName(CITY_NAME)
                .withValue("Seattle").build());
        SpeechletResponse citySr = mainSpeechlet.onIntent(
                IntentRequest.builder()
                        .withRequestId("test-request-id")
                        .withIntent(
                                Intent.builder()
                                        .withName(SET_CITY)
                                        .withSlots(citySlots)
                                        .build()
                        )
                        .build(),
                session
        );
        String citySpoken = ((PlainTextOutputSpeech) citySr.getOutputSpeech()).getText();
        assertThat(citySpoken, startsWith("Ok, your stop number is " + newStopCode + " in the " + TEST_REGION_1.getName() + " region. " +
                "Great.  I am ready to tell you about the next bus."));
        //After known region, ASK_STATE is cleared
        assertThat(session.getAttribute(ASK_STATE), equalTo(AskState.NONE.toString()));
    }

    @Test
    public void setStopBeforeUnknownCity() throws SpeechletException, IOException {
        String newStopCode = "2245";

        new Expectations() {{
            obaClient.getAllRegions();
            ArrayList<ObaRegion> regions = new ArrayList<>(1);
            regions.add(TEST_REGION_1);
            regions.add(TEST_REGION_2);
            regions.add(TEST_REGION_1);
            result = regions;
        }};

        //Session attributes from initial SET_STOP_NUMBER call
        session.setAttribute(ASK_STATE, AskState.STOP_BEFORE_CITY.toString());
        session.setAttribute(STOP_ID, newStopCode);

        HashMap<String, Slot> citySlots = new HashMap<>();
        citySlots.put(CITY_NAME, Slot.builder()
                .withName(CITY_NAME)
                .withValue("Houston").build());
        SpeechletResponse citySr = mainSpeechlet.onIntent(
                IntentRequest.builder()
                        .withRequestId("test-request-id")
                        .withIntent(
                                Intent.builder()
                                        .withName(SET_CITY)
                                        .withSlots(citySlots)
                                        .build()
                        )
                        .build(),
                session
        );
        String citySpoken = ((PlainTextOutputSpeech) citySr.getOutputSpeech()).getText();
        assertThat(citySpoken, containsString("OneBusAway could not locate a OneBusAway " +
                "region near Houston, the city you gave. " +
                "Supported regions include " + TEST_REGION_1.getName() + ", " +
                TEST_REGION_1.getName() + ", and " + TEST_REGION_2.getName() + ". " +
                "Tell me again, what's the largest city near you?"));
        //after unknown city, ASK_STATE is still STOP_BEFORE_CITY
        assertThat(session.getAttribute(ASK_STATE), equalTo(AskState.STOP_BEFORE_CITY.toString()));
    }

    @Test
    public void getArrivalsBeforeOnboard() throws SpeechletException, IOException {
        SpeechletResponse sr = mainSpeechlet.onIntent(
                IntentRequest.builder()
                        .withRequestId("test-request-id")
                        .withIntent(
                                Intent.builder()
                                        .withName(GET_ARRIVALS)
                                        .withSlots(new HashMap<String, Slot>())
                                        .build()
                        )
                        .build(),
                session
        );
        String spoken = ((PlainTextOutputSpeech)sr.getOutputSpeech()).getText();
        assertWelcomeResponse(spoken);
    }

    @Test
    public void getStopBeforeOnboard() throws SpeechletException, IOException {
        SpeechletResponse sr = mainSpeechlet.onIntent(
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
        assertWelcomeResponse(spoken);
    }

    @Test
    public void repeatBeforeOnboard() throws SpeechletException, IOException {
        SpeechletResponse sr = mainSpeechlet.onIntent(
                IntentRequest.builder()
                        .withRequestId("test-request-id")
                        .withIntent(
                                Intent.builder()
                                        .withName(REPEAT)
                                        .withSlots(new HashMap<String, Slot>())
                                        .build()
                        )
                        .build(),
                session
        );
        String spoken = ((PlainTextOutputSpeech)sr.getOutputSpeech()).getText();
        assertWelcomeResponse(spoken);
    }

    @Test
    public void goodbye() throws SpeechletException, IOException {
        TestUtil.assertGoodbye(mainSpeechlet, session);
    }

    @Test
    public void allIntents() throws SpeechletException, IOException, IllegalAccessException {
        new Expectations() {{
            obaClient.getAllRegions();
            ArrayList<ObaRegion> regions = new ArrayList<>(1);
            regions.add(TEST_REGION_1);
            regions.add(TEST_REGION_2);
            regions.add(TEST_REGION_1);
            result = regions;
        }};

        TestUtil.assertAllIntents(mainSpeechlet, session);
    }
}
