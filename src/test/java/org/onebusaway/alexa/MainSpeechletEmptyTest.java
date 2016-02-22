package org.onebusaway.alexa;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.*;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import config.UnitTests;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onebusaway.alexa.lib.GoogleMaps;
import org.onebusaway.alexa.storage.ObaDao;
import org.onebusaway.alexa.storage.ObaUserDataItem;
import org.onebusaway.location.Location;
import org.springframework.test.annotation.DirtiesContext;
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
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class MainSpeechletEmptyTest {
    static final LaunchRequest launchRequest = LaunchRequest.builder().withRequestId("test-req-id").build();

    @BeforeClass
    public static void setUpMocksForSpringConfiguration() {
        new MockUp<GoogleMaps>() {
            @Mock
            Optional<Location> geocode(String cityName) {
                return Optional.of(new Location("test"));
            }
        };
        new MockUp<ObaDao>() {
            @Mock
            Optional<ObaUserDataItem> getUserData(Session s) {
                return Optional.empty();
            }
        };
    }

    @Mocked
    GoogleMaps googleMaps;

    @Mocked
    ObaDao obaDao;

    @Resource
    Session session;

    @Resource
    MainSpeechlet mainSpeechlet;

    @Test
    public void launchAsksForCity() throws SpeechletException {
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

    @Test
    public void setRecognizableCity() throws SpeechletException {
        new Expectations() {{
            googleMaps.geocode("Seattle"); result = Optional.of(new Location("test provider"));
        }};
        HashMap<String, Slot> slots = new HashMap<>();
        slots.put("cityName", Slot.builder()
                                  .withName("cityName")
                                  .withValue("Seattle").build());
        SpeechletResponse sr = mainSpeechlet.onIntent(
                IntentRequest.builder()
                .withRequestId("test-request-id")
                .withIntent(
                        Intent.builder()
                        .withName("SetCityIntent")
                        .withSlots(slots)
                        .build()
                )
                .build(),
                session
        );
        String spoken = ((PlainTextOutputSpeech)sr.getOutputSpeech()).getText();
        assertThat(spoken, startsWith("Ok, you live in Seattle.  What's your stop number?"));
    }

    @Test
    public void setStopBeforeCity() throws SpeechletException {
        HashMap<String, Slot> slots = new HashMap<>();
        slots.put("stopNumber", Slot.builder()
                .withName("stopNumber")
                .withValue("2245").build());
        SpeechletResponse sr = mainSpeechlet.onIntent(
                IntentRequest.builder()
                        .withRequestId("test-request-id")
                        .withIntent(
                                Intent.builder()
                                        .withName("SetStopNumberIntent")
                                        .withSlots(slots)
                                        .build()
                        )
                        .build(),
                session
        );
        String spoken = ((PlainTextOutputSpeech)sr.getOutputSpeech()).getText();
        assertThat(spoken, startsWith("Welcome to OneBusAway! Let's set you up."));
    }
}
