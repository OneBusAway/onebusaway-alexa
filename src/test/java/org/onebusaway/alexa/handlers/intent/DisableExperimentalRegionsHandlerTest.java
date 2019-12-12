package org.onebusaway.alexa.handlers.intent;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.onebusaway.alexa.handlers.TestBase;
import org.onebusaway.alexa.util.CityUtil;
import org.onebusaway.alexa.util.StorageUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@PrepareForTest({
        DisableExperimentalRegionsHandler.class,
        StorageUtil.class,
        CityUtil.class
})
public class DisableExperimentalRegionsHandlerTest extends TestBase {
    @InjectMocks
    private DisableExperimentalRegionsHandler disableExperimentalRegionsHandler = new DisableExperimentalRegionsHandler();

    private static final String DISABLE_EXPERIMENTAL_REGIONS_INTENT_NAME = "DisableExperimentalRegions";

    @Before
    public void setup() {
        PowerMockito.mockStatic(StorageUtil.class);
        PowerMockito.mockStatic(CityUtil.class);
    }

    @Test
    public void getIntentRequestName_withoutInput_getRequestName() {
        assertEquals(DISABLE_EXPERIMENTAL_REGIONS_INTENT_NAME, disableExperimentalRegionsHandler.getIntentRequestName());
    }

    @Test
    public void handle_withObaData_getExperimentalRegionsDisabledMessage() throws Exception {
        when(obaUserClient.getAllRegions(anyBoolean())).thenReturn(Collections.emptyList());
        disableExperimentalRegionsHandler.handle(this.handlerInput);
        verify(promptHelper, times(1)).getResponse(anyString());
    }

    @Test
    public void handle_withoutObaData_askForCityResponse() {
        PowerMockito.mockStatic(CityUtil.class);
        withoutObaData();
        disableExperimentalRegionsHandler.handle(this.handlerInput);
        PowerMockito.verifyStatic(CityUtil.class);
        CityUtil.askForCityResponse();
    }
}
