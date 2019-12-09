package org.onebusaway.alexa.handlers.intent;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.onebusaway.alexa.handlers.TestBase;
import org.onebusaway.alexa.util.CityUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;

@PrepareForTest({
        CityUtil.class
})
public class GetArrivalsIntentHandlerTest extends TestBase {
    @InjectMocks
    private GetArrivalsIntentHandler getArrivalsIntentHandler = new GetArrivalsIntentHandler();

    private static final String GET_ARRIVALS_INTENT_NAME = "GetArrivalsIntent";

    @Test
    public void getIntentRequestName_withoutInput_getRequestName() {
        assertEquals(GET_ARRIVALS_INTENT_NAME, getArrivalsIntentHandler.getIntentRequestName());
    }

    @Test
    public void handle_withObaData_tellArrivalsResponse() {
        PowerMockito.mockStatic(CityUtil.class);
        getArrivalsIntentHandler.handle(this.handlerInput);
        PowerMockito.verifyStatic(CityUtil.class);
        CityUtil.tellArrivals(any(),any(), any(), any());
    }

    @Test
    public void handle_withoutObaData_askForCityResponse() {
        withoutObaData();
        PowerMockito.mockStatic(CityUtil.class);
        getArrivalsIntentHandler.handle(this.handlerInput);
        PowerMockito.verifyStatic(CityUtil.class);
        CityUtil.askForCityResponse();
    }
}
