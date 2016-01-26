package org.onebusaway.alexa;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.*;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import config.UnitTests;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.onebusaway.alexa.storage.ObaDao;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class,
                      classes = UnitTests.class)
public class MainSpeechletEmptyTest {
    static final LaunchRequest launchRequest = LaunchRequest.builder().withRequestId("test-req-id").build();
    static final Session session = Session.builder().withSessionId("test-session-id").build();

    @Resource
    ObaDao obaDao;

    @Resource
    MainSpeechlet mainSpeechlet;

    @Before
    public void setUpDao() {
        Mockito.reset(obaDao);
        Mockito.when(obaDao.getUserData(session)).thenReturn(Optional.empty());
    }

    @Test
    public void launchAsksForCity() throws SpeechletException {
        System.err.println("launchAsksForCity has " + mainSpeechlet + " for mainSpeechlet");
        SpeechletResponse sr = mainSpeechlet.onLaunch(
                launchRequest,
                session);

        assertThat(sr.getOutputSpeech(), is(instanceOf(PlainTextOutputSpeech.class)));
        String spoken = ((PlainTextOutputSpeech)sr.getOutputSpeech()).getText();
        assertThat(spoken, containsString("Welcome to "));
        assertThat(spoken, containsString("In what city do you live?"));
    }

    @Test
    public void getCity() throws SpeechletException {
        SpeechletResponse sr = mainSpeechlet.onIntent(
                IntentRequest.builder()
                .withRequestId("test-request-id")
                .withIntent(
                        Intent.builder()
                        .withName("GetCityIntent")
                        .withSlots(new HashMap<String, Slot>())
                        .build()
                )
                .build(),
                session
        );
        assertThat(sr.getOutputSpeech(), is(instanceOf(PlainTextOutputSpeech.class)));
        String spoken = ((PlainTextOutputSpeech)sr.getOutputSpeech()).getText();
        assertThat(spoken, containsString("You have not yet told me where you live."));
    }
}
