/*
 * Copyright 2016 Philip M. White (philip@mailworks.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.alexa.config;

import com.amazon.speech.speechlet.SpeechletException;
import org.onebusaway.alexa.AnonSpeechlet;
import org.onebusaway.alexa.AuthedSpeechlet;
import org.onebusaway.alexa.MainSpeechlet;
import org.onebusaway.alexa.lib.ObaAgencies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.stereotype.Component;

@PropertySource("classpath:/onebusaway.properties")
@ComponentScan(basePackages={"org.onebusaway.alexa.config"})
@Component
public class ApplicationConfig {
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

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
