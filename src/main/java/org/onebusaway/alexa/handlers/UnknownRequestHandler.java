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

package org.onebusaway.alexa.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import lombok.extern.log4j.Log4j;
import org.onebusaway.alexa.helper.PromptHelper;

import javax.inject.Inject;
import java.util.Optional;

import static org.onebusaway.alexa.constant.Prompt.UNKNOWN_INTENT_MESSAGE;

/**
 * Handler for unrecognized intent request.
 */
@Log4j
public class UnknownRequestHandler implements RequestHandler {
    @Inject
    private PromptHelper promptHelper;


    /**
     * Always returns true for intent that hasn't been processed.
     * @param handlerInput request received from Alexa
     * @return always true
     */
    @Override
    public boolean canHandle(HandlerInput handlerInput) {
        return true;
    }

    /**
     * Handle request that hasn't been handled by all handlers in OneBusAway.
     * @param handlerInput request received from Alexa
     * @return alexa response
     */
    @Override
    public Optional<Response> handle(HandlerInput handlerInput) {
        log.error("Unable to handle unrecognized Alexa request");
        log.info(handlerInput.getRequestEnvelopeJson());
        return promptHelper.getResponse(UNKNOWN_INTENT_MESSAGE);
    }
}