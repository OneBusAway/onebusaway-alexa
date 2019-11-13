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

package org.onebusaway.alexa.handlers.intent;

import com.amazon.ask.attributes.AttributesManager;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.LaunchRequest;
import com.amazon.ask.model.RequestEnvelope;
import com.amazon.ask.model.Session;
import com.amazon.ask.model.User;
import com.fasterxml.jackson.databind.node.TextNode;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.onebusaway.alexa.config.SpringContext;
import org.onebusaway.alexa.constant.SessionAttribute;
import org.onebusaway.alexa.helper.PromptHelper;
import org.onebusaway.alexa.lib.GoogleMaps;
import org.onebusaway.alexa.lib.ObaClient;
import org.onebusaway.alexa.lib.ObaUserClient;
import org.onebusaway.alexa.storage.ObaDao;
import org.onebusaway.alexa.storage.ObaUserDataItem;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.HashMap;
import java.util.Optional;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.onebusaway.alexa.constant.SessionAttribute.ASK_STATE;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
        ObaUserClient.class,
        SpringContext.class
})
abstract public class IntentRequestTestBase {
    @Mock
    protected ObaDao obaDao;
    @Mock
    protected GoogleMaps googleMaps;
    @Mock
    protected ObaClient obaClient;
    @Mock
    protected PromptHelper promptHelper;
    @Mock
    protected HandlerInput handlerInput;
    @Mock
    protected AttributesManager attributesManager;
    @Mock
    protected ObaUserDataItem obaUserDataItem;
    @Mock
    protected ObaUserClient obaUserClient;
    @Mock
    protected AnnotationConfigApplicationContext annotationConfigApplicationContext;
    @Spy
    protected HashMap<String, Object> sessionAttributes = new HashMap();

    protected RequestEnvelope requestEnvelope;

    protected Session session;

    protected User user;

    protected static final String SSML_FORMAT = "<speak>%s</speak>";

    protected static final String USER_ID = "userId";
    protected static final String CITY_NAME = "cityName";
    protected static final String STOP_ID = "stopId";
    protected static final long REGION_ID = 1;
    protected static final String REGION_NAME = "regionName";
    protected static final String OBA_BASE_URL = "https://busurl.com";
    protected static final String PREVIOUS_RESPONSE = "previousResponse";
    protected static final long LAST_ACCESS_TIME = 2;
    protected static final long CLOCK_TIME = 0;
    protected static final String TIME_ZONE = "timeZone";
    protected static final long ANNOUNCED_INTRODUCTION = 0;
    protected static final long ANNOUNCED_FEATURES_V1_1_0 = 0;
    protected static final boolean EXPERIMENTAL_REGIONS = false;

    @Before
    public void presetup() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockStaticUtils();
        mockHandlerInput();
        mockObaUserData();
        mockAlexaSessionAttribute();

        setup();
    }

    /**
     * Can be override by child class for customized initialization logic. not required if using the default mocks.
     */
    protected void setup() throws Exception {
    }

    /**
     * Helper method to mock static Utilities.
     */
    protected void mockStaticUtils() throws Exception{
        PowerMockito.mockStatic(SpringContext.class);
        PowerMockito.whenNew(ObaUserClient.class).withAnyArguments().thenReturn(obaUserClient);
        PowerMockito.when(SpringContext.getInstance()).thenReturn(annotationConfigApplicationContext);
    }

    /**
     * Helper method to mock handler input.
     */
    protected void mockHandlerInput() {
        user = User.builder().withUserId(USER_ID).build();
        session = Session.builder().withUser(user).build();
        requestEnvelope = RequestEnvelope.builder()
                .withRequest(LaunchRequest.builder().build())
                .withSession(session).build();
        when(handlerInput.getAttributesManager()).thenReturn(attributesManager);
        when(handlerInput.getRequestEnvelope()).thenReturn(requestEnvelope);
        when(handlerInput.getRequestEnvelopeJson()).thenReturn(new TextNode(StringUtils.EMPTY));
        when(obaDao.getUserData(eq(USER_ID))).thenReturn(Optional.of(obaUserDataItem));
        when(attributesManager.getSessionAttributes()).thenReturn(sessionAttributes);
    }

    /**
     * Helper method to mock handler input.
     */
    protected void mockObaUserData() {
        when(obaUserDataItem.getCity()).thenReturn(CITY_NAME);
        when(obaUserDataItem.getStopId()).thenReturn(STOP_ID);
        when(obaUserDataItem.getRegionId()).thenReturn(REGION_ID);
        when(obaUserDataItem.getRegionName()).thenReturn(REGION_NAME);
        when(obaUserDataItem.getObaBaseUrl()).thenReturn(OBA_BASE_URL);
        when(obaUserDataItem.getPreviousResponse()).thenReturn(PREVIOUS_RESPONSE);
        when(obaUserDataItem.getLastAccessTime()).thenReturn(LAST_ACCESS_TIME);
        when(obaUserDataItem.getSpeakClockTime()).thenReturn(CLOCK_TIME);
        when(obaUserDataItem.getTimeZone()).thenReturn(TIME_ZONE);
        when(obaUserDataItem.getAnnouncedIntroduction()).thenReturn(ANNOUNCED_INTRODUCTION);
        when(obaUserDataItem.getAnnouncedFeaturesv1_1_0()).thenReturn(ANNOUNCED_FEATURES_V1_1_0);
        when(obaUserDataItem.isExperimentalRegions()).thenReturn(EXPERIMENTAL_REGIONS);
        when(obaUserDataItem.getRegionName()).thenReturn(REGION_NAME);
    }

    /**
     * Helper method to mock Alexa session attribute
     */
    protected void mockAlexaSessionAttribute() {
        when(sessionAttributes.get(ASK_STATE)).thenReturn(SessionAttribute.AskState.NONE.toString());
    }

    /**
     * Helper method to return optional.empty when read ObaData.
     */
    protected void withoutObaData() {
        when(obaDao.getUserData(eq(USER_ID))).thenReturn(Optional.empty());
    }
}
