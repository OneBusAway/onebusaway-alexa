package org.onebusaway.alexa.handlers.intent;

import com.amazon.ask.model.Response;
import com.amazon.ask.model.ui.SsmlOutputSpeech;
import com.amazon.ask.response.ResponseBuilder;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.onebusaway.alexa.constant.Prompt;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;


public class YesIntentHandlerTest extends IntentRequestTestBase {
    @InjectMocks
    private YesIntentHandler yesIntentHandler = new YesIntentHandler();

    private static final String YES_INTENT_NAME = "AMAZON.YesIntent";
    private static final String ERROR_MESSAGE = "errorMessage";

    @Test
    public void getIntentRequestName_withoutInput_getRequestName() {
        assertEquals(YES_INTENT_NAME, yesIntentHandler.getIntentRequestName());
    }

    @Test
    public void handle_askStateIsNone_getGeneralMessageResponse() {
        when(promptHelper.getResponse(eq(Prompt.GENERAL_ERROR_MESSAGE)))
                .thenReturn(new ResponseBuilder().withSpeech(ERROR_MESSAGE).build());
        Optional<Response> response = yesIntentHandler.handle(this.handlerInput);
        assertTrue(response.isPresent());
        assertEquals(String.format(SSML_FORMAT, ERROR_MESSAGE), ((SsmlOutputSpeech) response.get().getOutputSpeech()).getSsml());
    }
}
