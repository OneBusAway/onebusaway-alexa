package config;

import com.amazon.speech.speechlet.SpeechletException;
import org.mockito.Mockito;
import org.onebusaway.alexa.AnonSpeechlet;
import org.onebusaway.alexa.AuthedSpeechlet;
import org.onebusaway.alexa.MainSpeechlet;
import org.onebusaway.alexa.lib.GoogleMaps;
import org.onebusaway.alexa.lib.ObaAgencies;
import org.onebusaway.alexa.lib.ObaClient;
import org.onebusaway.alexa.storage.ObaDao;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UnitTests {
    @Bean
    public ObaDao obaDao() {
        return Mockito.mock(ObaDao.class);
    }

    @Bean
    public GoogleMaps googleMaps() {
        return Mockito.mock(GoogleMaps.class);
    }

    @Bean
    public ObaClient obaClient() {
        return Mockito.mock(ObaClient.class);
    }

    @Bean
    public MainSpeechlet mainSpeechlet() {
        return new MainSpeechlet();
    }

    @Bean
    public AnonSpeechlet anonSpeechlet() {
        return new AnonSpeechlet();
    }

    @Bean
    public AuthedSpeechlet authedSpeechlet() { return new AuthedSpeechlet(); }

    @Bean
    public ObaAgencies obaAgencies() throws SpeechletException {
        return new ObaAgencies(getClass().getResourceAsStream("/oba-agencies.yml"));
    }
}
