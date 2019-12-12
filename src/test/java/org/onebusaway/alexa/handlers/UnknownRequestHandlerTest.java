package org.onebusaway.alexa.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Response;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.onebusaway.alexa.constant.Prompt;
import org.onebusaway.alexa.helper.PromptHelper;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.when;

public class UnknownRequestHandlerTest {
    @Mock
    private PromptHelper promptHelper;

    @Mock
    private HandlerInput handlerInput;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @InjectMocks
    private UnknownRequestHandler unknownRequestHandler;

    @Test
    public void canHandle_withAnyCondition_returnsTrue() {
        assertTrue(unknownRequestHandler.canHandle(null));
    }

    @Test
    public void handle_withAnyInput_unknownIntentMessage() {
        when(promptHelper.getResponse(Prompt.UNKNOWN_INTENT_MESSAGE)).thenReturn(Optional.empty());
        unknownRequestHandler.handle(handlerInput);
        Mockito.verify(promptHelper).getResponse(Mockito.eq(Prompt.UNKNOWN_INTENT_MESSAGE));
    }
}
