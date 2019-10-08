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

import com.amazon.ask.dispatcher.exception.ExceptionHandler;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Response;
import lombok.extern.log4j.Log4j;

import java.util.Optional;

/**
 * GenericException handler will be invoked when there is a exception while skill process the request.
 */
@Log4j
public class GenericExceptionHandler implements ExceptionHandler {
    /**
     * Always returns true for all exceptions.
     * @param handlerInput
     * @param throwable
     * @return
     */
    @Override
    public boolean canHandle(final HandlerInput handlerInput, final Throwable throwable) {
        return true;
    }

    /**
     * Handle the exception request.
     * @param handlerInput
     * @param throwable
     * @return
     */
    @Override
    public Optional<Response> handle(final HandlerInput handlerInput, final Throwable throwable) {
        String errorMessage = throwable.getMessage();
        return handlerInput.getResponseBuilder().withSpeech(errorMessage)
                .withShouldEndSession(true)
                .build();
    }
}
