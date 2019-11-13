/*
 * Copyright 2016-2019 Sean J. Barbeau (sjbarbeau@gmail.com),
 * Philip M. White (philip@mailworks.org)
 * Chunzhang Mo (victormocz@gmail.com)
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

package org.onebusaway.alexa.helper;

import com.amazon.ask.model.Response;
import com.amazon.ask.response.ResponseBuilder;
import lombok.Setter;
import lombok.extern.log4j.Log4j;
import org.onebusaway.alexa.constant.Prompt;
import org.onebusaway.alexa.personalization.PersonalizationInfo;
import org.springframework.context.support.ResourceBundleMessageSource;

import javax.inject.Inject;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * Helper class to get strings and prompts.
 */
@Log4j
public class PromptHelper {
    @Inject
    private ResourceBundleMessageSource resourceBundleMessageSource;

    @Inject
    private Locale locale;

    @Setter
    private PersonalizationInfo personalizationStatus;

    private static String SPR_SSML_FORMAT = "<alexa:name type='first' personId='%s'/>";

    public static String ADDRESS_SSML_FORMAT = "<say-as interpret-as='address'>%s</say-as>";

    /**
     * Get the prompt string, it will replace the parameters with the string placeholder in resource bundle.
     *
     * @param prompt speech prompt
     * @param parameters parameters to replace the placeholder in string
     * @return the string in resource bundle
     */
    public String getPrompt(final Prompt prompt, final String... parameters) {
        String response = "";
        if (personalizationStatus != null && personalizationStatus.isPersonalized() && prompt.isPersonalizedPrompt()) {
            final String personName = String.format(SPR_SSML_FORMAT, personalizationStatus.getPrincipleId());
            String[] parameterWithName = new String[parameters.length + 1];
            parameterWithName[0] = personName;
            IntStream.range(1, parameterWithName.length)
                    .forEach(idx -> parameterWithName[idx] = parameters[idx - 1]);
            response = resourceBundleMessageSource.getMessage(prompt.getPersonalizedResourceId(), parameterWithName, locale);
        } else {
            response = resourceBundleMessageSource.getMessage(prompt.getResourceId(), parameters, locale);
        }
        log.info("Get Response: " + response);
        return response;
    }

    /**
     * Get the prompt string.
     *
     * @param prompt speech prompt
     * @return the string in resource bundle
     */
    public String getPrompt(Prompt prompt) {
        String response = "";
        if (personalizationStatus != null && personalizationStatus.isPersonalized() && prompt.isPersonalizedPrompt()) {
            final String personName = String.format(SPR_SSML_FORMAT, personalizationStatus.getPrincipleId());
            response = resourceBundleMessageSource.getMessage(prompt.getPersonalizedResourceId(), new String[]{personName}, locale);
        } else {
            response = resourceBundleMessageSource.getMessage(prompt.getResourceId(), null, locale);
        }
        log.info("Get Response: " + response);
        return response;
    }

    /**
     * Helper method to create the Alexa response with prompt and reprompt,
     * should end session flag will be set to false.
     *
     * @param prompt speech prompt
     * @param reprompt reprompt to reask user for response
     * @return alexa response with speech, reprompt and should end session flag set to false
     */
    public Optional<Response> getResponse(final Prompt prompt, final Prompt reprompt) {
        return new ResponseBuilder()
                .withSpeech(getPrompt(prompt))
                .withShouldEndSession(false)
                .withReprompt(getPrompt(reprompt))
                .build();
    }

    /**
     * Helper method to create the Alexa response with prompt,
     * should end session flag will be set to true.
     *
     * @param prompt speech prompt
     * @return alexa response with speech and should end session flag set to true
     */
    public Optional<Response> getResponse(final Prompt prompt) {
        return new ResponseBuilder()
                .withSpeech(getPrompt(prompt))
                .withShouldEndSession(true)
                .build();
    }

    /**
     * Helper method to create Alexa response with speech text,
     * should end session flag will be set to true.
     *
     * @param speech
     * @return alexa response with speech and should end session flag set to true
     */
    public Optional<Response> getResponse(final String speech) {
        return new ResponseBuilder()
                .withSpeech(speech)
                .withShouldEndSession(true)
                .build();
    }

    /**
     * Helper method to create Alexa response with speech text and reprompt text,
     * should end session flag will be set to false.
     *
     * @param speech
     * @param reprompt
     * @return alexa response with speech, reprompt and should end session flag set to false
     */
    public Optional<Response> getResponse(final String speech, final String reprompt) {
        return new ResponseBuilder()
                .withSpeech(speech)
                .withReprompt(reprompt)
                .withShouldEndSession(false)
                .build();
    }
}
