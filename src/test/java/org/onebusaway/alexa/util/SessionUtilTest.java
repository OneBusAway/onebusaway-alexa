package org.onebusaway.alexa.util;

import com.amazon.ask.attributes.AttributesManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class SessionUtilTest {
    @Mock
    private AttributesManager attributesManager;

    @Spy
    private HashMap<String, Object> sessionAttribute;

    private static final String ATTRIBUTE_KEY = "attributeKey";
    private static final String ATTRIBUTE_VALUE = "attributeValue";
    private static final String INVALID_ATTRIBUTE_KEY = "invalidAttributeKey";
    private static final String WRONG_TYPE_ATTRIBUTE_KEY = "wrongTypeAttributeKey";
    private static final String DEFAULT_ATTRIBUTE_VALUE = "defaultAttributeValue";

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        when(attributesManager.getSessionAttributes()).thenReturn(sessionAttribute);
    }

    @Test
    public void getSessionAttribute_withAttributeInSession_returnsAttribute() {
        when(sessionAttribute.get(ATTRIBUTE_KEY)).thenReturn(ATTRIBUTE_VALUE);
        String output = SessionUtil.getSessionAttribute(attributesManager, ATTRIBUTE_KEY, String.class);
        assertEquals(ATTRIBUTE_VALUE, output);
    }

    @Test
    public void getSessionAttribute_withWrongTypeOfAttributeInSession_returnsNull() {
        when(sessionAttribute.get(WRONG_TYPE_ATTRIBUTE_KEY)).thenReturn(new ArrayList());
        String output = SessionUtil.getSessionAttribute(attributesManager, ATTRIBUTE_KEY, String.class);
        assertEquals(null, output);
    }

    @Test
    public void getSessionAttribute_withoutAttributeInSession_returnsNull() {
        when(sessionAttribute.get(INVALID_ATTRIBUTE_KEY)).thenReturn(null);
        String output = SessionUtil.getSessionAttribute(attributesManager, ATTRIBUTE_KEY, String.class);
        assertEquals(null, output);
    }

    @Test
    public void getSessionAttribute_withAttributeInSessionWithDefaultValue_returnsAttribute() {
        when(sessionAttribute.get(ATTRIBUTE_KEY)).thenReturn(ATTRIBUTE_VALUE);
        String output = SessionUtil.getSessionAttribute(attributesManager, ATTRIBUTE_KEY, String.class, DEFAULT_ATTRIBUTE_VALUE);
        assertEquals(ATTRIBUTE_VALUE, output);
    }

    @Test
    public void getSessionAttribute_withWrongTypeOfAttributeInSessionWithDefaultValue_returnsDefaultAttribute() {
        when(sessionAttribute.get(WRONG_TYPE_ATTRIBUTE_KEY)).thenReturn(new ArrayList());
        String output = SessionUtil.getSessionAttribute(attributesManager, ATTRIBUTE_KEY, String.class, DEFAULT_ATTRIBUTE_VALUE);
        assertEquals(DEFAULT_ATTRIBUTE_VALUE, output);
    }

    @Test
    public void getSessionAttribute_withoutAttributeInSessionWithDefaultValue_returnsDefaultAttribute() {
        when(sessionAttribute.get(INVALID_ATTRIBUTE_KEY)).thenReturn(null);
        String output = SessionUtil.getSessionAttribute(attributesManager, ATTRIBUTE_KEY, String.class, DEFAULT_ATTRIBUTE_VALUE);
        assertEquals(DEFAULT_ATTRIBUTE_VALUE, output);
    }

}
