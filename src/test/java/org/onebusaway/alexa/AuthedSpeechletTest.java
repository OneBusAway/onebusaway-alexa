package org.onebusaway.alexa;

import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import config.UnitTests;
import mockit.Expectations;
import mockit.Mocked;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onebusaway.alexa.lib.ObaUserClient;
import org.onebusaway.alexa.storage.ObaUserDataItem;
import org.onebusaway.io.client.elements.ObaArrivalInfo;
import org.onebusaway.io.client.request.ObaArrivalInfoResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import javax.annotation.Resource;

import java.net.URISyntaxException;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class,
        classes = UnitTests.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AuthedSpeechletTest {
    static final LaunchRequest launchRequest = LaunchRequest.builder().withRequestId("test-req-id").build();

    @Mocked
    ObaArrivalInfo obaArrivalInfo;

    @Mocked
    ObaArrivalInfoResponse obaArrivalInfoResponse;

    @Mocked
    ObaUserClient obaUserClient;

    @Resource
    Session session;

    @Resource
    AuthedSpeechlet authedSpeechlet;

    @Resource
    ObaUserDataItem testUserData;

    @Before
    public void initializeAuthedSpeechlet() throws URISyntaxException {
        authedSpeechlet.setUserData(testUserData);
    }

    @Test
    public void launchTellsArrivals() throws SpeechletException {
        ObaArrivalInfo[] obaArrivalInfoArray = new ObaArrivalInfo[1];
        obaArrivalInfoArray[0] = obaArrivalInfo;
        new Expectations() {{
            obaArrivalInfo.getShortName(); result = "8";
            obaArrivalInfo.getHeadsign(); result = "Mlk Way Jr";
            obaArrivalInfoResponse.getArrivalInfo(); result = obaArrivalInfoArray;
            obaUserClient.getArrivalsAndDeparturesForStop(anyString, anyInt); result = obaArrivalInfoResponse;
        }};

        SpeechletResponse sr = authedSpeechlet.onLaunch(
                launchRequest,
                session);

        String spoken = ((PlainTextOutputSpeech)sr.getOutputSpeech()).getText();
        assertThat(spoken, equalTo("Route 8 Mlk Way Jr is now arriving based on the schedule -- "));
    }

    @Test
    public void noUpcomingArrivals() throws SpeechletException {
        ObaArrivalInfo[] obaArrivalInfoArray = new ObaArrivalInfo[0];
        new Expectations() {{
            obaArrivalInfoResponse.getArrivalInfo(); result = obaArrivalInfoArray;
            obaUserClient.getArrivalsAndDeparturesForStop(anyString, anyInt); result = obaArrivalInfoResponse;
        }};

        SpeechletResponse sr = authedSpeechlet.onLaunch(
                launchRequest,
                session);
        String spoken = ((PlainTextOutputSpeech)sr.getOutputSpeech()).getText();
        assertThat(spoken, equalTo("There are no upcoming arrivals at your stop for the next "
                + AuthedSpeechlet.ARRIVALS_SCAN_MINS + " minutes."));
    }
}
