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

@PrepareForTest({
        DisableClockTimeHandler.class,
        StorageUtil.class,
        CityUtil.class
})
public class DisableClockTimeHandlerTest extends TestBase {
    @InjectMocks
    private DisableClockTimeHandler disableClockTimeHandler = new DisableClockTimeHandler();

    @Captor
    private ArgumentCaptor<ObaUserDataItem> obaUserDataItemArgumentCaptor;

    private static final String DISABLE_CLOCK_TIME_INTENT_NAME = "DisableClockTime";

    @Test
    public void getIntentRequestName_withoutInput_getRequestName() {
        assertEquals(DISABLE_CLOCK_TIME_INTENT_NAME, disableClockTimeHandler.getIntentRequestName());
    }

    @Test
    public void handle_withObaData_getClockTimeDisabledMessage() {
        PowerMockito.mockStatic(StorageUtil.class);
        disableClockTimeHandler.handle(this.handlerInput);
        Mockito.verify(obaDao).saveUserData(obaUserDataItemArgumentCaptor.capture());
        assertEquals(0, obaUserDataItemArgumentCaptor.getValue().getSpeakClockTime());
        Mockito.verify(promptHelper).getPrompt(Mockito.eq(Prompt.DISABLE_CLOCK_TIME));
        PowerMockito.verifyStatic(StorageUtil.class);
        StorageUtil.saveOutputForRepeat(anyString(), any(), any());
    }

    @Test
    public void handle_withoutObaData_askForCityResponse() {
        PowerMockito.mockStatic(CityUtil.class);
        withoutObaData();
        disableClockTimeHandler.handle(this.handlerInput);
        PowerMockito.verifyStatic(CityUtil.class);
        CityUtil.askForCityResponse();
    }
}
