/*
 * Copyright 2016 Sean J. Barbeau (sjbarbeau@gmail.com),
 * Philip M. White (philip@mailworks.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package config;

import com.amazon.speech.speechlet.Session;
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
