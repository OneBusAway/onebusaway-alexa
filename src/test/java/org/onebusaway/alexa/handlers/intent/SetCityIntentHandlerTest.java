package org.onebusaway.alexa.handlers.intent;

import com.amazon.ask.model.Intent;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.RequestEnvelope;
import com.amazon.ask.model.Slot;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onebusaway.alexa.constant.SessionAttribute;
import org.onebusaway.alexa.handlers.TestBase;
import org.onebusaway.alexa.util.CityUtil;
import org.onebusaway.io.client.elements.ObaRegionElement;
import org.onebusaway.location.Location;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;
import static org.onebusaway.alexa.constant.SessionAttribute.ASK_STATE;

@PrepareForTest({
        CityUtil.class
})
public class SetCityIntentHandlerTest extends TestBase {
    @InjectMocks
    private SetCityIntentHandler setCityIntentHandler = new SetCityIntentHandler();

    @Mock
    private ObaRegionElement obaRegionElement;

    private static final String SET_CITY_INTENT_NAME = "SetCityIntent";
    private static final String CITY_NAME_KEY = "cityName";
    private static final String LOCATION_NAME = "locationName";
    private static final String OBA_URL = "https://obaurl.org";

    @Test
    public void getIntentRequestName_withoutInput_getRequestName() {
        assertEquals(SET_CITY_INTENT_NAME, setCityIntentHandler.getIntentRequestName());
    }

    @Test
    public void handle_withoutCityNameInRequest_askForCityResponse() {
        PowerMockito.mockStatic(CityUtil.class);
        setCityIntentHandler.handle(this.handlerInput);
        PowerMockito.verifyStatic(CityUtil.class);
        CityUtil.askForCityResponse();
    }

    @Test
    public void handle_withCityNameInRequestAndLocationIsNotPresent_askForCityResponse() {
        PowerMockito.mockStatic(CityUtil.class);
        mockIntentRequest();
        Mockito.when(googleMaps.geocode(any())).thenReturn(Optional.empty());
        setCityIntentHandler.handle(this.handlerInput);
        PowerMockito.verifyStatic(CityUtil.class);
        CityUtil.askForCityResponse();
    }

    @Test
    public void handle_withCityNameInRequestAndLocationIsPresentButNotSupported_cityNotSupport() throws Exception {
        PowerMockito.mockStatic(CityUtil.class);
        mockIntentRequest();
        Mockito.when(googleMaps.geocode(any())).thenReturn(Optional.of(new Location(LOCATION_NAME)));
        Mockito.when(obaClient.getClosestRegion(any(), anyBoolean())).thenReturn(Optional.empty());
        setCityIntentHandler.handle(this.handlerInput);
        PowerMockito.verifyStatic(CityUtil.class);
        CityUtil.askForCityResponse(anyString(), any(), any());
    }

    @Test
    public void handle_withCityNameInRequestAndLocationIsPresent_getResponse() throws Exception {
        mockIntentRequest();
        Mockito.when(googleMaps.geocode(any())).thenReturn(Optional.of(new Location(LOCATION_NAME)));
        Mockito.when(obaClient.getClosestRegion(any(), anyBoolean())).thenReturn(Optional.of(obaRegionElement));
        Mockito.when(obaRegionElement.getObaBaseUrl()).thenReturn(OBA_URL);
        setCityIntentHandler.handle(this.handlerInput);
        Mockito.verify(promptHelper).getResponse(anyString(), anyString());
    }

    @Test
    public void handle_withCityNameInRequestAndLocationIsPresentStateEqualsStopBeforeCity_fulfillCityAndStop() throws Exception {
        PowerMockito.mockStatic(CityUtil.class);
        when(sessionAttributes.get(ASK_STATE)).thenReturn(SessionAttribute.AskState.STOP_BEFORE_CITY.toString());
        mockIntentRequest();
        Mockito.when(googleMaps.geocode(any())).thenReturn(Optional.of(new Location(LOCATION_NAME)));
        Mockito.when(obaClient.getClosestRegion(any(), anyBoolean())).thenReturn(Optional.of(obaRegionElement));
        Mockito.when(obaRegionElement.getObaBaseUrl()).thenReturn(OBA_URL);
        setCityIntentHandler.handle(this.handlerInput);
        PowerMockito.verifyStatic(CityUtil.class);
        CityUtil.fulfillCityAndStop(anyString(), any(), any(), any(), any());
    }

    private void mockIntentRequest() {
        Map<String, Slot> slots = new HashMap<>();
        slots.put(CITY_NAME_KEY, Slot.builder().withValue(CITY_NAME).build());
        Intent intent = Intent.builder().withSlots(slots).build();
        IntentRequest intentRequest = IntentRequest.builder().withIntent(intent).build();
        requestEnvelope = RequestEnvelope.builder().withContext(context).withRequest(intentRequest).withSession(session).build();
        Mockito.when(handlerInput.getRequestEnvelope()).thenReturn(requestEnvelope);
        Mockito.when(handlerInput.getRequest()).thenReturn(intentRequest);
    }
}
