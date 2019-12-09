package org.onebusaway.alexa.personalization;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class PersonalizationInfoTest {
    private static final String PRINCIPLE_ID = "principleId";
    private static final String USER_ID = "userId";
    private static final String PERSON_ID = "personId";
    private static final boolean IS_PERSONALIZED = true;

    @Test
    public void init() {
        PersonalizationInfo personalizationInfo = new PersonalizationInfo(PRINCIPLE_ID, USER_ID, PERSON_ID, IS_PERSONALIZED);

        assertEquals(PRINCIPLE_ID, personalizationInfo.getPrincipleId());
        assertEquals(USER_ID, personalizationInfo.getUserId());
        assertEquals(PERSON_ID, personalizationInfo.getPersonId());
        assertEquals(IS_PERSONALIZED, personalizationInfo.isPersonalized());
    }
}
