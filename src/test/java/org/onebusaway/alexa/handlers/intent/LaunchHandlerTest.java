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

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.LaunchRequest;
import com.amazon.ask.model.RequestEnvelope;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.onebusaway.alexa.constant.Prompt;
import org.onebusaway.alexa.handlers.TestBase;
import org.onebusaway.alexa.util.CityUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@PrepareForTest({
        CityUtil.class
})
public class LaunchHandlerTest extends TestBase {
    @Captor
    private ArgumentCaptor<Prompt> argumentCaptor;
    @InjectMocks
    private LaunchRequestHandler launchRequestHandler;

    private static final String LAUNCH_REQUEST_NAME = "LaunchRequest";

    @Override
    protected void setup() throws Exception {
        PowerMockito.mockStatic(CityUtil.class);
    }

    @Test
    public void getIntentRequestName_withoutInput_getRequestName() {
        assertEquals(LAUNCH_REQUEST_NAME, launchRequestHandler.getIntentRequestName());
    }

    @Test
    public void canHandle_withLauchRequest_returnsTrue() {
        requestEnvelope = RequestEnvelope.builder().withRequest(LaunchRequest.builder().build()).build();
        handlerInput = HandlerInput.builder().withRequestEnvelope(requestEnvelope).build();
        assertTrue(launchRequestHandler.canHandle(handlerInput));
    }

    @Test
    public void handleWithoutObaData_cityNotInSession_askForCityResponse() {
        withoutObaData();
        when(sessionAttributes.get(CITY_NAME)).thenReturn(null);
        launchRequestHandler.handle(handlerInput);
        verify(promptHelper).getResponse(argumentCaptor.capture(), argumentCaptor.capture());
        assertEquals(Prompt.WELCOME_MESSAGE, argumentCaptor.getAllValues().get(0));
        assertEquals(Prompt.ASK_FOR_CITY, argumentCaptor.getAllValues().get(1));
    }

    @Test
    public void handleWithoutObaData_cityInSession_askForCityResponse() {
        withoutObaData();
        when(sessionAttributes.get(CITY_NAME)).thenReturn(CITY_NAME);
        launchRequestHandler.handle(handlerInput);
        verify(promptHelper).getResponse(argumentCaptor.capture(), argumentCaptor.capture());
        assertEquals(Prompt.REASK_FOR_STOP, argumentCaptor.getAllValues().get(0));
        assertEquals(Prompt.ASK_FOR_STOP, argumentCaptor.getAllValues().get(1));
    }
}
