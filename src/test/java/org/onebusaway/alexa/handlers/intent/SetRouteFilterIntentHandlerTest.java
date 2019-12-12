package org.onebusaway.alexa.handlers.intent;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.onebusaway.alexa.handlers.TestBase;
import org.onebusaway.alexa.util.CityUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;

import static org.junit.Assert.assertEquals;

@PrepareForTest({
        CityUtil.class
})
public class SetRouteFilterIntentHandlerTest extends TestBase {
    @InjectMocks
    private SetRouteFilterIntentHandler setRouteFilterIntentHandler = new SetRouteFilterIntentHandler();

    private static final String SET_ROUTE_FILTER_INTENT_NAME = "SetRouteFilter";

    @Test
    public void getIntentRequestName_withoutInput_getRequestName() {
        assertEquals(SET_ROUTE_FILTER_INTENT_NAME, setRouteFilterIntentHandler.getIntentRequestName());
    }

    @Test
    public void handle_withoutObaData_askForCityResponse() {
        PowerMockito.mockStatic(CityUtil.class);
        withoutObaData();
        setRouteFilterIntentHandler.handle(handlerInput);
        PowerMockito.verifyStatic(CityUtil.class);
        CityUtil.askForCityResponse();
    }
}
