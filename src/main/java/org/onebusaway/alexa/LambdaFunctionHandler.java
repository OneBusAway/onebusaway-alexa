/*
 * Copyright (C) 2016 Sean J. Barbeau (sjbarbeau@gmail.com),
 *                    Philip M. White (philip@mailworks.org)
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
package org.onebusaway.alexa;

import java.util.HashSet;
import java.util.Set;

import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler;
import org.onebusaway.alexa.config.ApplicationConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * OneBusAway Alexa - main handler to receive and process messages from Alexa
 * via Lambda
 */
public class LambdaFunctionHandler extends SpeechletRequestStreamHandler {
    private static AnnotationConfigApplicationContext context;
    private static final Set<String> supportedApplicationIds = new HashSet<>();

    static {
        context = new AnnotationConfigApplicationContext(ApplicationConfig.class);
        /*
         * This Id can be found on https://developer.amazon.com/edw/home.html#/ "Edit" the relevant
         * Alexa Skill and put the relevant Application Ids in this Set.
         */
        supportedApplicationIds.add(
                context.getEnvironment().getProperty("skill-app-id")
        );
    }

    public LambdaFunctionHandler() {
        super(context.getBean(MainSpeechlet.class), supportedApplicationIds);
    }
}
