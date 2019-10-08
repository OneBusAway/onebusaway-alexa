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

public class StopIntentHandlerTest extends IntentRequestTestBase {
    @InjectMocks
    private StopIntentHandler stopIntentHandler = new StopIntentHandler();

    private static final String STOP_INTENT_NAME = "AMAZON.StopIntent";
    private static final String USER_EXIT_MESSAGE = "userExit";

    @Test
    public void getIntentRequestName_withoutInput_getRequestName() {
        assertEquals(STOP_INTENT_NAME, stopIntentHandler.getIntentRequestName());
    }

    @Test
    public void handle_withoutObaData_hearExitMessage() {
        when(obaDao.getUserData(eq(USER_ID))).thenReturn(Optional.empty());
        when(promptHelper.getResponse(eq(Prompt.USER_EXIT)))
                .thenReturn(new ResponseBuilder().withSpeech(USER_EXIT_MESSAGE).build());
        Optional<Response> response = stopIntentHandler.handle(this.handlerInput);
        assertTrue(response.isPresent());
        assertEquals(String.format(SSML_FORMAT, USER_EXIT_MESSAGE), ((SsmlOutputSpeech) response.get().getOutputSpeech()).getSsml());
    }

    @Test
    public void handle_withObaData_hearExitMessage() {
        when(promptHelper.getResponse(eq(Prompt.USER_EXIT)))
                .thenReturn(new ResponseBuilder().withSpeech(USER_EXIT_MESSAGE).build());
        Optional<Response> response = stopIntentHandler.handle(this.handlerInput);
        assertTrue(response.isPresent());
        assertEquals(String.format(SSML_FORMAT, USER_EXIT_MESSAGE), ((SsmlOutputSpeech) response.get().getOutputSpeech()).getSsml());
    }
}

