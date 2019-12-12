package org.onebusaway.alexa.handlers.intent;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.onebusaway.alexa.constant.Prompt;
import org.onebusaway.alexa.handlers.TestBase;
import org.onebusaway.alexa.util.StorageUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;
import static org.onebusaway.alexa.constant.Prompt.GET_CITY_IN_SESSION_EMPTY;
import static org.onebusaway.alexa.constant.Prompt.GET_CITY_IN_SESSION_EXIST;

@PrepareForTest({
        StorageUtil.class
})
public class GetCityIntentHandlerTest extends TestBase {
    @InjectMocks
    private GetCityIntentHandler getCityIntentHandler = new GetCityIntentHandler();

    @Captor
    private ArgumentCaptor<Prompt> argumentCaptor;

    private static final String GET_CITY_INTENT_NAME = "GetCityIntent";

    @Test
    public void getIntentRequestName_withoutInput_getRequestName() {
        assertEquals(GET_CITY_INTENT_NAME, getCityIntentHandler.getIntentRequestName());
    }

    @Test
    public void handle_withObaData_getCityResponse() {
        PowerMockito.mockStatic(StorageUtil.class);
        getCityIntentHandler.handle(this.handlerInput);
        Mockito.verify(promptHelper).getPrompt(argumentCaptor.capture(), any(), any());
        assertEquals(Prompt.GET_CITY, argumentCaptor.getValue());
        PowerMockito.verifyStatic(StorageUtil.class);
        StorageUtil.saveOutputForRepeat(anyString(), any(), any());
    }

    @Test
    public void handle_withoutObaData_getCityResponse() {
        withoutObaData();
        getCityIntentHandler.handle(this.handlerInput);
        Mockito.verify(promptHelper).getPrompt(eq(GET_CITY_IN_SESSION_EMPTY));
    }

    @Test
    public void handle_withoutObaData_getCityResponseSessionExist() {
        withoutObaData();
        when(sessionAttributes.get(CITY_NAME)).thenReturn(CITY_NAME);
        getCityIntentHandler.handle(this.handlerInput);
        Mockito.verify(promptHelper).getPrompt(eq(GET_CITY_IN_SESSION_EXIST), anyString());
    }

}
