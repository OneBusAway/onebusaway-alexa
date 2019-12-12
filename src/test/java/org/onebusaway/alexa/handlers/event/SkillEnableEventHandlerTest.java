package org.onebusaway.alexa.handlers.event;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.RequestEnvelope;
import com.amazon.ask.model.events.skillevents.SkillEnabledRequest;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.onebusaway.alexa.handlers.TestBase;
import org.onebusaway.alexa.storage.ObaUserEnableItem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SkillEnableEventHandlerTest extends TestBase {
    @InjectMocks
    private SkillEnableEventHandler skillEnableEventHandler = new SkillEnableEventHandler();

    @Captor
    ArgumentCaptor<ObaUserEnableItem> obaUserEnableItemArgumentCaptor;

    private final static String ENABLE_EVENT_NAME = "AlexaSkillEvent.SkillEnabled";

    @Test
    public void getEventRequestName_withoutInput_returnsTheEventName() {
        assertEquals(ENABLE_EVENT_NAME, skillEnableEventHandler.getEventRequestName());
    }

    @Test
    public void canHandle_withSkillEnabledRequest_returnsTrue() {
        requestEnvelope = RequestEnvelope.builder().withRequest(SkillEnabledRequest.builder().build()).build();
        handlerInput = HandlerInput.builder().withRequestEnvelope(requestEnvelope).build();
        assertTrue(skillEnableEventHandler.canHandle(this.handlerInput));
    }

    @Test
    public void handle_withSkillEnabledRequest_recordsSavedToDB() {
        requestEnvelope = RequestEnvelope.builder().withRequest(SkillEnabledRequest.builder().build()).withContext(context).build();
        handlerInput = HandlerInput.builder().withRequestEnvelope(requestEnvelope).build();
        skillEnableEventHandler.handle(this.handlerInput);
        Mockito.verify(obaDao).saveUserEnableData(obaUserEnableItemArgumentCaptor.capture());
        assertEquals(USER_ID, obaUserEnableItemArgumentCaptor.getValue().getUserId());
    }
}
