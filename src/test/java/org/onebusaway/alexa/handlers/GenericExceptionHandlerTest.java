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

package org.onebusaway.alexa.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.ui.SsmlOutputSpeech;
import com.amazon.ask.response.ResponseBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class GenericExceptionHandlerTest {
    private GenericExceptionHandler genericExceptionHandler =
            new GenericExceptionHandler();

    private static final String ERROR_MESSAGE = "errorMessage";
    private static final String SSML_SPEAK_TAG_FORMAT = "<speak>%s</speak>";

    @Mock
    private HandlerInput handlerInput;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void canHandle_withInput_returnsTrue() {
        assertTrue(genericExceptionHandler.canHandle(handlerInput, new Exception()));
    }

    @Test
    public void handle_withException_returnErrorMessage() {
        when(handlerInput.getResponseBuilder()).thenReturn(new ResponseBuilder());
        Exception e = new Exception(ERROR_MESSAGE);
        Optional<Response> response = genericExceptionHandler.handle(handlerInput, e);
        assertTrue(response.isPresent());
        SsmlOutputSpeech ssmlOutputSpeech = (SsmlOutputSpeech) response.get().getOutputSpeech();
        assertEquals(String.format(SSML_SPEAK_TAG_FORMAT, ERROR_MESSAGE), ssmlOutputSpeech.getSsml());
    }
}
