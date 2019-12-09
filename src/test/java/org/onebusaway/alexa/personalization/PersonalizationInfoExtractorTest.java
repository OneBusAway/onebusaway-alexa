package org.onebusaway.alexa.personalization;

import com.amazon.ask.model.Context;
import com.amazon.ask.model.LaunchRequest;
import com.amazon.ask.model.RequestEnvelope;
import com.amazon.ask.model.Session;
import com.amazon.ask.model.interfaces.system.SystemState;
import org.junit.Test;
import org.onebusaway.alexa.exception.OneBusAwayException;
import org.onebusaway.alexa.handlers.TestBase;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class PersonalizationInfoExtractorTest extends TestBase {

    @Test(expected = OneBusAwayException.class)
    public void extractPersonalizationStatusFromAlexaRequest_withNullInput_oneBusAwayExceptionThrown() {
        PersonalizationInfoExtractor.extractPersonalizationStatusFromAlexaRequest(null);
    }

    @Test
    public void extractPersonalizationStatusFromAlexaRequest_withPersonInRequest_personalized() {
        system = SystemState.builder().withUser(user).withPerson(person).build();
        context = Context.builder().withSystem(system).build();
        session = Session.builder().withUser(user).build();
        requestEnvelope = RequestEnvelope.builder()
                .withRequest(LaunchRequest.builder().build())
                .withContext(context)
                .withSession(session).build();
        when(handlerInput.getRequestEnvelope()).thenReturn(requestEnvelope);

        PersonalizationInfo personalizationInfo = PersonalizationInfoExtractor.extractPersonalizationStatusFromAlexaRequest(this.handlerInput);
        assertEquals(USER_ID,personalizationInfo.getUserId());
        assertEquals(PERSON_ID, personalizationInfo.getPrincipleId());
        assertTrue(personalizationInfo.isPersonalized());
    }

    @Test
    public void extractPersonalizationStatusFromAlexaRequest_withoutPersonInRequest_personalized() {
        PersonalizationInfo personalizationInfo = PersonalizationInfoExtractor.extractPersonalizationStatusFromAlexaRequest(this.handlerInput);
        assertEquals(USER_ID,personalizationInfo.getUserId());
        assertEquals(USER_ID, personalizationInfo.getPrincipleId());
        assertFalse(personalizationInfo.isPersonalized());
    }
}
