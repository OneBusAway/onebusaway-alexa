package org.onebusaway.alexa.handlers.intent;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.onebusaway.alexa.constant.Prompt;
import org.onebusaway.alexa.handlers.TestBase;
import org.onebusaway.alexa.util.CityUtil;
import org.onebusaway.alexa.util.StorageUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;

@PrepareForTest({
        CityUtil.class,
})
public class HelpIntentHandlerTest extends TestBase {
    @InjectMocks
    private HelpIntentHandler helpIntentHandler = new HelpIntentHandler();

    private static final String HELP_INTENT_NAME = "AMAZON.HelpIntent";

    @Test
    public void getIntentRequestName_withoutInput_getRequestName() {
        assertEquals(HELP_INTENT_NAME, helpIntentHandler.getIntentRequestName());
    }

    @Test
    public void handle_withObaData_getHelpMessage() {
        helpIntentHandler.handle(this.handlerInput);
        Mockito.verify(promptHelper).getResponse(eq(Prompt.HELP_MESSAGE));
    }

    @Test
    public void handle_withoutObaData_askForCityResponse() {
        withoutObaData();
        PowerMockito.mockStatic(CityUtil.class);
        helpIntentHandler.handle(this.handlerInput);
        PowerMockito.verifyStatic(CityUtil.class);
        CityUtil.askForCityResponse();
    }
}
