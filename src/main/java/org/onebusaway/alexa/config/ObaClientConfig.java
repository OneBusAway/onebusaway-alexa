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

import org.onebusaway.alexa.lib.ObaClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.onebusaway.alexa.config.ObaProperty.ONEBUSAWAY_MAPS_API_KEY;

@Configuration
public class ObaClientConfig {
    @Value("${" + ONEBUSAWAY_MAPS_API_KEY + "}")
    private String apiKey;

    @Bean
    public ObaClient obaClient() {
        return new ObaClient(apiKey);
    }
}
