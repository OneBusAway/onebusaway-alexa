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
package org.onebusaway.alexa;

import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.User;
import config.UnitTests;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onebusaway.alexa.storage.ObaUserDataItem;
import org.onebusaway.alexa.util.CityUtil;
import org.onebusaway.alexa.util.SessionUtil;
import org.onebusaway.io.client.elements.ObaRegion;
import org.onebusaway.io.client.elements.ObaRegionElement;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import javax.annotation.Resource;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.onebusaway.alexa.SessionAttribute.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class,
        classes = UnitTests.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UtilTest {
    static final String TEST_USER_ID = "test-user-id";
    static final User testUser = User.builder().withUserId(TEST_USER_ID).build();
    Session session;

    private static final ObaRegion TEST_REGION_1 = new ObaRegionElement(
            1,
            "Tampa",
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
    public void populateSession() throws SpeechletException {
        // Mock persisted user data
        ObaUserDataItem userData = new ObaUserDataItem(
                TEST_USER_ID,
                "Puget Sound",
                "6497",
                TEST_REGION_2.getId(),
                TEST_REGION_2.getName(),
                TEST_REGION_2.getObaBaseUrl(),
                "",
                System.currentTimeMillis(),
                0,
                "America/Los_Angeles",
                new HashMap<>(),
                1L,
                1L,
                false,
                null
        );

        Session emptySession = Session.builder()
                .withUser(testUser)
                .withSessionId("test-session-id")
                .build();

        SessionUtil.populateAttributes(emptySession, Optional.of(userData));

        assertEquals(userData.getRegionName(), emptySession.getAttribute(CITY_NAME));
        assertEquals(userData.getStopId(), emptySession.getAttribute(STOP_ID));
        assertEquals(userData.getRegionId(), emptySession.getAttribute(REGION_ID));
        assertEquals(userData.getRegionName(), emptySession.getAttribute(REGION_NAME));
        assertEquals(userData.getObaBaseUrl(), emptySession.getAttribute(OBA_BASE_URL));
        assertEquals(userData.getPreviousResponse(), emptySession.getAttribute(PREVIOUS_RESPONSE));
        assertEquals(userData.getLastAccessTime(), emptySession.getAttribute(LAST_ACCESS_TIME));
        assertEquals(userData.getSpeakClockTime(), emptySession.getAttribute(CLOCK_TIME));
        assertEquals(userData.getTimeZone(), emptySession.getAttribute(TIME_ZONE));
        assertEquals(userData.getAnnouncedIntroduction(), emptySession.getAttribute(ANNOUNCED_INTRODUCTION));
        assertEquals(userData.getAnnouncedFeaturesv1_1_0(), emptySession.getAttribute(ANNOUNCED_FEATURES_V1_1_0));
    }

    @Test
    public void getRegionListText() {
        ArrayList<ObaRegion> regions = new ArrayList<>();
        regions.add(TEST_REGION_1);
        regions.add(TEST_REGION_2);
        regions.add(TEST_REGION_3);
        regions.add(TEST_REGION_EXPERIMENTAL);

        String productionRegions = CityUtil.allRegionsSpoken(regions, false);
        assertEquals("Supported regions include Atlanta, Puget Sound, and Tampa. ", productionRegions);

        String allRegions = CityUtil.allRegionsSpoken(regions, false);
        assertEquals("Supported regions include Atlanta, Boston, Puget Sound, and Tampa. ", allRegions);
    }
}
