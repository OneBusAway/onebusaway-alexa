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
        EnableExperimentalRegionsHandler.class,
        StorageUtil.class,
        CityUtil.class
})
public class EnableExperimentalRegionsHandlerTest extends TestBase {
    @InjectMocks
    private EnableExperimentalRegionsHandler enableExperimentalRegionsHandler = new EnableExperimentalRegionsHandler();

    private static final String ENABLE_EXPERIMENTAL_REGIONS_INTENT_NAME = "EnableExperimentalRegions";

    @Before
    public void setup() {
        PowerMockito.mockStatic(StorageUtil.class);
        PowerMockito.mockStatic(CityUtil.class);
    }

    @Test
    public void getIntentRequestName_withoutInput_getRequestName() {
        assertEquals(ENABLE_EXPERIMENTAL_REGIONS_INTENT_NAME, enableExperimentalRegionsHandler.getIntentRequestName());
    }

    @Test
    public void handle_withObaData_getExperimentalRegionsDisabledMessage() throws Exception {
        when(obaUserClient.getAllRegions(anyBoolean())).thenReturn(Collections.emptyList());
        enableExperimentalRegionsHandler.handle(this.handlerInput);
        verify(promptHelper, times(1)).getResponse(anyString());
    }

    @Test
    public void handle_withoutObaData_askForCityResponse() {
        PowerMockito.mockStatic(CityUtil.class);
        withoutObaData();
        enableExperimentalRegionsHandler.handle(this.handlerInput);
        PowerMockito.verifyStatic(CityUtil.class);
        CityUtil.askForCityResponse();
    }
}
