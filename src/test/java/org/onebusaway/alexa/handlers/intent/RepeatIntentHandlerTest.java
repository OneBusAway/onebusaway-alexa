package org.onebusaway.alexa.handlers.intent;

import com.amazon.ask.model.Response;
import com.amazon.ask.model.ui.SsmlOutputSpeech;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.onebusaway.alexa.config.PromptsConfig;
import org.onebusaway.alexa.constant.Prompt;
import org.onebusaway.alexa.helper.PromptHelper;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RepeatIntentHandlerTest extends IntentRequestTestBase {
    @Captor
    private ArgumentCaptor<String> argumentCaptor;

    @InjectMocks
    private RepeatIntentHandler repeatIntentHandler;

    private static final String REPEAT_INTENT_NAME = "AMAZON.RepeatIntent";
    private static final String PREVIOUS_RESPONSE_KEY = "previousResponse";

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
        when(obaDao.getUserData(eq(USER_ID))).thenReturn(Optional.empty());
        Optional<Response> response = repeatIntentHandler.handle(handlerInput);
        assertTrue(response.isPresent());
        SsmlOutputSpeech ssmlOutputSpeech = (SsmlOutputSpeech) response.get().getOutputSpeech();
        String expetecdWelcomeMessage =
                new AnnotationConfigApplicationContext(PromptsConfig.class).getBean("promptHelper", PromptHelper.class).getPrompt(Prompt.WELCOME_MESSAGE);
        assertEquals(String.format(SSML_FORMAT, expetecdWelcomeMessage), ssmlOutputSpeech.getSsml());
    }
}
