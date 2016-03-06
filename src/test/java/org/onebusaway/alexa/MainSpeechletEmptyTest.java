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
                Location l = new Location("test");
                l.setLatitude(47.6097);
                l.setLongitude(-122.3331);
                return Optional.of(l);
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
    public void help() throws SpeechletException {
        SpeechletResponse sr = mainSpeechlet.onIntent(
                IntentRequest.builder()
                        .withRequestId("test-request-id")
                        .withIntent(
                                Intent.builder()
                                        .withName("AMAZON.HelpIntent")
                                        .withSlots(new HashMap<String, Slot>())
                                        .build()
                        )
                        .build(),
                session
        );
        String spoken = ((PlainTextOutputSpeech)sr.getOutputSpeech()).getText();
        assertThat(spoken, containsString("Start by telling me your city."));
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
    public void setRecognizableCitySeattle() throws SpeechletException {
        new Expectations() {{
            googleMaps.geocode("Seattle");
            Location l = new Location("test");
            l.setLatitude(47.6097);
            l.setLongitude(-122.3331);
            result = Optional.of(l);
        }};
        HashMap<String, Slot> slots = new HashMap<>();
        slots.put(SessionAttributes.CITY_NAME.toString(), Slot.builder()
                                  .withName(SessionAttributes.CITY_NAME.toString())
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
        assertThat(spoken, startsWith("Ok, we found the Puget Sound region near you.  What's your stop number?"));
    }

    @Test
    public void setRecognizableCityTampa() throws SpeechletException {
        new Expectations() {{
            googleMaps.geocode("Tampa");
            Location l = new Location("test");
            l.setLatitude(27.9681);
            l.setLongitude(-82.4764);
            result = Optional.of(l);
        }};
        HashMap<String, Slot> slots = new HashMap<>();
        slots.put(SessionAttributes.CITY_NAME.toString(), Slot.builder()
                .withName(SessionAttributes.CITY_NAME.toString())
                .withValue("Tampa").build());
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
        assertThat(spoken, startsWith("Ok, we found the Tampa region near you.  What's your stop number?"));
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
