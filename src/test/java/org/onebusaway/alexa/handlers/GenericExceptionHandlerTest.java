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
