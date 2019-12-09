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

import com.amazon.ask.model.Response;
import com.amazon.ask.model.ui.SsmlOutputSpeech;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.onebusaway.alexa.config.PromptsConfig;
import org.onebusaway.alexa.constant.Prompt;
import org.onebusaway.alexa.handlers.TestBase;
import org.onebusaway.alexa.helper.PromptHelper;
import org.onebusaway.alexa.util.CityUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@PrepareForTest({
        CityUtil.class
})
public class RepeatHandlerTest extends TestBase {
    @Captor
    private ArgumentCaptor<String> argumentCaptor;

    @InjectMocks
    private RepeatIntentHandler repeatIntentHandler;

    private static final String REPEAT_INTENT_NAME = "AMAZON.RepeatIntent";
    private static final String PREVIOUS_RESPONSE_KEY = "previousResponse";

    @Override
    protected void setup() throws Exception {
        PowerMockito.mockStatic(CityUtil.class);
        when(CityUtil.askForCityResponse()).thenReturn(Optional.empty());
    }

    @Test
    public void getIntentRequestName_withoutInput_getRequestName() {
        assertEquals(REPEAT_INTENT_NAME, repeatIntentHandler.getIntentRequestName());
    }

    @Test
    public void handle_responseInSession_returnThePreviousResponseInSession() {
        when(sessionAttributes.get(PREVIOUS_RESPONSE_KEY)).thenReturn(PREVIOUS_RESPONSE);
        repeatIntentHandler.handle(handlerInput);
        verify(promptHelper).getResponse(argumentCaptor.capture());
        assertEquals(PREVIOUS_RESPONSE, argumentCaptor.getValue());
    }

    @Test
    public void handle_responseInObaData_returnThePreviousResponseInSession() {
        when(sessionAttributes.get(PREVIOUS_RESPONSE_KEY)).thenReturn(null);
        repeatIntentHandler.handle(handlerInput);
        verify(promptHelper).getResponse(argumentCaptor.capture());
        assertEquals(PREVIOUS_RESPONSE, argumentCaptor.getValue());
    }

    @Test
    public void handle_withObaData_returnAskForCityResponse() {
        when(sessionAttributes.get(PREVIOUS_RESPONSE_KEY)).thenReturn(null);
        repeatIntentHandler.handle(handlerInput);
        verify(promptHelper).getResponse(argumentCaptor.capture());
        assertEquals(PREVIOUS_RESPONSE, argumentCaptor.getValue());
    }

    @Test
    public void handle_withoutObaData_returnAskForCityResponse() {
        PromptHelper promptHelper = new AnnotationConfigApplicationContext(PromptsConfig.class).getBean("promptHelper", PromptHelper.class);
        when(obaDao.getUserData(eq(USER_ID))).thenReturn(Optional.empty());
        when(CityUtil.askForCityResponse()).thenReturn(promptHelper.getResponse(Prompt.WELCOME_MESSAGE, Prompt.ASK_FOR_CITY));
        Optional<Response> response = repeatIntentHandler.handle(handlerInput);
        assertTrue(response.isPresent());
        SsmlOutputSpeech ssmlOutputSpeech = (SsmlOutputSpeech) response.get().getOutputSpeech();
        String expetecdWelcomeMessage =
                promptHelper.getPrompt(Prompt.WELCOME_MESSAGE);
        assertEquals(String.format(SSML_FORMAT, expetecdWelcomeMessage), ssmlOutputSpeech.getSsml());
    }
}
