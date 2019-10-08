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

package org.onebusaway.alexa.handlers;

import com.amazon.ask.attributes.AttributesManager;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Request;
import com.amazon.ask.model.RequestEnvelope;
import com.amazon.ask.model.Response;
import com.amazon.ask.request.Predicates;
import lombok.extern.log4j.Log4j;
import org.onebusaway.alexa.storage.ObaDao;

import javax.inject.Inject;
import java.util.Optional;

/**
 * BaseHandler for all alexa requests (e.g. intent, event and etc.).
 */
@Log4j
abstract public class BaseHandler implements RequestHandler {
    @Inject
    protected ObaDao obaDao;

    protected HandlerInput handlerInput;

    protected AttributesManager attributesManager;

    /**
     * Gets the intent name that handler can handle.
     *
     * @return
     */
    abstract public String getRequestName();

    /**
     * Determines whether the giving handler can handle the request.
     *
     * @param handlerInput
     * @return true if the request can be handled, otherwise false
     */
    @Override
    public boolean canHandle(final HandlerInput handlerInput) {
        boolean canHandleRequest = handlerInput.matches(Predicates.intentName(getRequestName()));
        final String requestType = Optional.ofNullable(handlerInput)
                .map(HandlerInput::getRequestEnvelope)
                .map(RequestEnvelope::getRequest)
                .map(Request::getType)
                .orElse("unkonwnType");
        log.info(String.format("%s handler canHandle returns %s for %s", this.getClass().getSimpleName(), canHandleRequest, requestType));
        return canHandleRequest;
    }

    /**
     * Process the giving input and return the alexa response(speech, reprompt, audio and etc).
     *
     * @param handlerInput
     * @return
     */
    @Override
    public Optional<Response> handle(final HandlerInput handlerInput) {
        try {
            this.handlerInput = handlerInput;
            this.attributesManager = handlerInput.getAttributesManager();
            log.info(handlerInput.getRequestEnvelopeJson());
            return handle();
        } catch (Exception e) {
            log.error("Exception thrown while processing the request", e);
            //Let the exception handler to handle the request
            throw e;
        }
    }

    /**
     * Process the giving input and return the alexa response(speech, reprompt, audio and etc).
     *
     * @return
     */
    public abstract Optional<Response> handle();
}
