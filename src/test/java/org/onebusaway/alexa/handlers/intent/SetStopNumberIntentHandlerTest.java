package org.onebusaway.alexa.handlers.intent;

import com.amazon.ask.model.Intent;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.RequestEnvelope;
import com.amazon.ask.model.Slot;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.onebusaway.alexa.constant.Prompt;
import org.onebusaway.alexa.handlers.TestBase;
import org.onebusaway.alexa.util.CityUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;

@PrepareForTest({
        CityUtil.class
})
public class SetStopNumberIntentHandlerTest extends TestBase {
    @InjectMocks
    private SetStopNumberIntentHandler setStopNumberIntentHandler = new SetStopNumberIntentHandler();

    private static final String SET_STOP_NUMBER_INTENT_NAME = "SetStopNumberIntent";
    private static final String STOP_NUMBER_KEY = "stopNumber";

    @Test
    public void getIntentRequestName_withoutInput_getRequestName() {
        assertEquals(SET_STOP_NUMBER_INTENT_NAME, setStopNumberIntentHandler.getIntentRequestName());
    }

    @Test
    public void handle_stopIdNotInRequest_getStopNumber() {
        setStopNumberIntentHandler.handle(this.handlerInput);
        Mockito.verify(promptHelper).getResponse(eq(Prompt.LOOKING_FOR_STOP_NUMBER), eq(Prompt.ASK_FOR_STOP));
    }

    @Test
    public void handle_stopIdInRequest_getStopNumber() {
        PowerMockito.mockStatic(CityUtil.class);
        Map<String, Slot> slots = new HashMap<>();
        slots.put(STOP_NUMBER_KEY, Slot.builder().withValue(STOP_ID).build());
        Intent intent = Intent.builder().withSlots(slots).build();
        IntentRequest intentRequest = IntentRequest.builder().withIntent(intent).build();
        requestEnvelope = RequestEnvelope.builder().withContext(context).withRequest(intentRequest).withSession(session).build();
        Mockito.when(handlerInput.getRequestEnvelope()).thenReturn(requestEnvelope);
        Mockito.when(handlerInput.getRequest()).thenReturn(intentRequest);
        setStopNumberIntentHandler.handle(this.handlerInput);
        PowerMockito.verifyStatic(CityUtil.class);
        CityUtil.fulfillCityAndStop(anyString(), any(), any(), any(), any());
    }
}
