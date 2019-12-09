package org.onebusaway.alexa.handlers.intent;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.onebusaway.alexa.handlers.TestBase;
import org.onebusaway.alexa.util.CityUtil;
import org.onebusaway.alexa.util.StorageUtil;
import org.onebusaway.io.client.request.ObaStopResponse;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.lang.reflect.Constructor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;

@PrepareForTest({
        CityUtil.class,
        StorageUtil.class
})
public class GetStopNumberIntentHandlerTest extends TestBase {
    @InjectMocks
    private GetStopNumberIntentHandler getStopNumberIntentHandler = new GetStopNumberIntentHandler();

    private static final String GET_STOP_NUMBER_INTENT_NAME = "GetStopNumberIntent";

    @Test
    public void getIntentRequestName_withoutInput_getRequestName() {
        assertEquals(GET_STOP_NUMBER_INTENT_NAME, getStopNumberIntentHandler.getIntentRequestName());
    }

    @Test
    public void handle_withoutObaData_askForCityResponse() {
        withoutObaData();
        PowerMockito.mockStatic(CityUtil.class);
        getStopNumberIntentHandler.handle(this.handlerInput);
        PowerMockito.verifyStatic(CityUtil.class);
        CityUtil.askForCityResponse();
    }
}
