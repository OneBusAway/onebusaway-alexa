package org.onebusaway.alexa.handlers.event;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.RequestEnvelope;
import com.amazon.ask.model.events.skillevents.SkillDisabledRequest;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.onebusaway.alexa.handlers.TestBase;
import org.onebusaway.alexa.storage.ObaUserRelationItem;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class SkillDisableEventHandlerTest extends TestBase {
    @InjectMocks
    private SkillDisableEventHandler skillDisableEventHandler = new SkillDisableEventHandler();

    private final static String DISABLE_EVENT_NAME = "AlexaSkillEvent.SkillDisabled";

    @Test
    public void getEventRequestName_withoutInput_returnsTheEventName() {
        assertEquals(DISABLE_EVENT_NAME, skillDisableEventHandler.getEventRequestName());
    }

    @Test
    public void canHandle_withSkillDisabledRequest_returnsTrue() {
        requestEnvelope = RequestEnvelope.builder().withRequest(SkillDisabledRequest.builder().build()).build();
        handlerInput = HandlerInput.builder().withRequestEnvelope(requestEnvelope).build();
        assertTrue(skillDisableEventHandler.canHandle(this.handlerInput));
    }

    @Test
    public void handle_withSkillDisabledRequest_recordsInDBRemoved() {
        List<ObaUserRelationItem> obaUserRelationItems = new LinkedList<>();
        obaUserRelationItems.add(new ObaUserRelationItem(USER_ID, PERSON_ID));
        requestEnvelope = RequestEnvelope.builder().withRequest(SkillDisabledRequest.builder().build()).withContext(context).build();
        when(obaDao.getObaUserRelations(anyString())).thenReturn(obaUserRelationItems);
        handlerInput = HandlerInput.builder().withRequestEnvelope(requestEnvelope).build();
        skillDisableEventHandler.handle(this.handlerInput);
        Mockito.verify(obaDao).getObaUserRelations(Mockito.eq(USER_ID));
        Mockito.verify(obaDao).removeAllUserDataItem(any());
    }
}
