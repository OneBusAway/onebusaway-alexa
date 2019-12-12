package org.onebusaway.alexa.handlers.intent;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.onebusaway.alexa.constant.Prompt;
import org.onebusaway.alexa.handlers.TestBase;
import org.onebusaway.alexa.storage.ObaUserDataItem;
import org.onebusaway.alexa.util.CityUtil;
import org.onebusaway.alexa.util.StorageUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@PrepareForTest({
        EnableClockTimeHandler.class,
        StorageUtil.class,
        CityUtil.class
})
public class EnableClockTimeHandlerTest extends TestBase {
    @InjectMocks
    private EnableClockTimeHandler enableClockTimeHandler = new EnableClockTimeHandler();

    @Captor
    private ArgumentCaptor<ObaUserDataItem> obaUserDataItemArgumentCaptor;

    private static final String ENABLE_CLOCK_TIME_INTENT_NAME = "EnableClockTime";

    @Test
    public void getIntentRequestName_withoutInput_getRequestName() {
        assertEquals(ENABLE_CLOCK_TIME_INTENT_NAME, enableClockTimeHandler.getIntentRequestName());
    }

    @Test
    public void handle_withObaData_getClockTimeDisabledMessage() {
        PowerMockito.mockStatic(StorageUtil.class);
        when(obaUserDataItem.getSpeakClockTime()).thenReturn(1L);
        enableClockTimeHandler.handle(this.handlerInput);
        Mockito.verify(obaDao).saveUserData(obaUserDataItemArgumentCaptor.capture());
        assertEquals(1L, obaUserDataItemArgumentCaptor.getValue().getSpeakClockTime());
        Mockito.verify(promptHelper).getPrompt(Mockito.eq(Prompt.ENABLE_CLOCK_TIME));
        PowerMockito.verifyStatic(StorageUtil.class);
        StorageUtil.saveOutputForRepeat(anyString(), any(), any());
    }

    @Test
    public void handle_withoutObaData_askForCityResponse() {
        PowerMockito.mockStatic(CityUtil.class);
        withoutObaData();
        enableClockTimeHandler.handle(this.handlerInput);
        PowerMockito.verifyStatic(CityUtil.class);
        CityUtil.askForCityResponse();
    }
}
