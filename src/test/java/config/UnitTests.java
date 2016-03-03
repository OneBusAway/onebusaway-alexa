package config;

import org.onebusaway.alexa.AuthedSpeechlet;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletException;
import org.onebusaway.alexa.AnonSpeechlet;
import org.onebusaway.alexa.AuthedSpeechlet;
import org.onebusaway.alexa.MainSpeechlet;
import org.onebusaway.alexa.lib.GoogleMaps;
import org.onebusaway.alexa.lib.ObaClient;
import org.onebusaway.alexa.storage.ObaDao;
import org.onebusaway.alexa.storage.ObaUserDataItem;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UnitTests {
    @Bean
    public ObaDao obaDao() {
        // this should be mocked in test class
        return new ObaDao(null);
    }

    @Bean
    public GoogleMaps googleMaps() {
        // this should be mocked in test class
        return new GoogleMaps("should have been mocked");
    }

    @Bean
    public ObaClient obaClient() {
        return new ObaClient("should have been mocked");
    }

    @Bean
    public MainSpeechlet mainSpeechlet() {
        // this should be mocked in test class
        return new MainSpeechlet();
    }

    @Bean
    public AnonSpeechlet anonSpeechlet() {
        // this should be mocked in test class
        return new AnonSpeechlet();
    }

    @Bean
    public ObaUserDataItem testUserData() {
        return new ObaUserDataItem("test-user-id", "Seattle", "test-stop-id", 1, "Puget Sound", "http://api.pugetsound.onebusaway.org/", null);
    }

    @Bean
    public AuthedSpeechlet authedSpeechlet() { return new AuthedSpeechlet(); }

    @Bean
    public Session session() {
        return Session.builder().withSessionId("test-session-id").build();
    }
}
