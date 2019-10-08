package org.onebusaway.alexa.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Intent;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.RequestEnvelope;
import com.amazon.ask.model.Response;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BaseHandlerTest extends BaseHandler {

    private static final String BASE_REQUEST_NAME = "BaseRequestName";

    private static final String RANDOM_REQUEST_NAME = "RandomRequestName";

    @Override
    public String getRequestName() {
        return BASE_REQUEST_NAME;
    }

    @Override
    public Optional<Response> handle() {
        return null;
    }

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void canHandle_withDifferentRequestName_returnsFalse() {
        Intent intent = Intent.builder().withName(RANDOM_REQUEST_NAME).build();
        IntentRequest intentRequest = IntentRequest.builder().withIntent(intent).build();
        RequestEnvelope requestEnvelope = RequestEnvelope.builder().withRequest(intentRequest).build();
        HandlerInput handlerInput = HandlerInput.builder().withRequestEnvelope(requestEnvelope).build();
        assertFalse(canHandle(handlerInput));
    }

    @Test
    public void canHandle_withSameRequestName_returnsTrue() {
        Intent intent = Intent.builder().withName(BASE_REQUEST_NAME).build();
        IntentRequest intentRequest = IntentRequest.builder().withIntent(intent).build();
        RequestEnvelope requestEnvelope = RequestEnvelope.builder().withRequest(intentRequest).build();
        HandlerInput handlerInput = HandlerInput.builder().withRequestEnvelope(requestEnvelope).build();
        assertTrue(canHandle(handlerInput));
    }
}
