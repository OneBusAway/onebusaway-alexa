/*
 * Copyright 2016 Sean J. Barbeau (sjbarbeau@gmail.com),
 * Philip M. White (philip@mailworks.org)
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onebusaway.alexa.config;

import org.onebusaway.alexa.helper.PromptHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.Locale;

/**
 * Spring config for resource bundle and prompt helper.
 */
@Configuration
public class PromptsConfig {

    /**
     * Helper class to get strings and prompts.
     *
     * @return
     */
    @Bean
    public PromptHelper promptHelper() {
        return new PromptHelper();
    }

    /**
     * Currently, OneBusAway is only supported in US and Canada, the default locale will be set to US.
     */
    @Bean
    public Locale defaultLocale() {
        return Locale.US;
    }

    /**
     * ResourceBundle to get string resource from config file.
     *
     * @return
     */
    @Bean
    public ResourceBundleMessageSource resourceBundleMessageSource() {
        ResourceBundleMessageSource resourceBundleMessageSource = new ResourceBundleMessageSource();
        resourceBundleMessageSource.setBasename("skillresponse/response");
        resourceBundleMessageSource.setDefaultEncoding("UTF-8");
        return resourceBundleMessageSource;
    }
}
