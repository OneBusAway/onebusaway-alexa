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
import com.amazon.ask.response.ResponseBuilder;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.onebusaway.alexa.constant.Prompt;
import org.onebusaway.alexa.handlers.TestBase;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

public class NoHandlerTest extends TestBase {
    @InjectMocks
    private NoIntentHandler noIntentHandler = new NoIntentHandler();

    private static final String NO_INTENT_NAME = "AMAZON.NoIntent";
    private static final String ERROR_MESSAGE = "errorMessage";

    @Test
    public void getIntentRequestName_withoutInput_getRequestName() {
        assertEquals(NO_INTENT_NAME, noIntentHandler.getIntentRequestName());
    }

    @Test
    public void handle_askStateIsNone_getGeneralMessageResponse() {
        when(promptHelper.getResponse(eq(Prompt.GENERAL_ERROR_MESSAGE)))
                .thenReturn(new ResponseBuilder().withSpeech(ERROR_MESSAGE).build());
        Optional<Response> response = noIntentHandler.handle(this.handlerInput);
        assertTrue(response.isPresent());
        assertEquals(String.format(SSML_FORMAT, ERROR_MESSAGE), ((SsmlOutputSpeech) response.get().getOutputSpeech()).getSsml());
    }
}
