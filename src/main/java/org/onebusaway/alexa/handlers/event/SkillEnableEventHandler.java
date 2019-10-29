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

package org.onebusaway.alexa.handlers.event;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Context;
import com.amazon.ask.model.RequestEnvelope;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.User;
import com.amazon.ask.model.events.skillevents.SkillEnabledRequest;
import com.amazon.ask.model.interfaces.system.SystemState;
import com.amazon.ask.request.Predicates;
import lombok.extern.log4j.Log4j;
import org.onebusaway.alexa.storage.ObaUserEnableItem;

import java.time.Instant;
import java.util.Optional;

/**
 * Handler for AlexaSkillEvent.SkillEnabled event request.
 * (e.g. skill enable event will be sent to this handler when user enable OneBusAway skill)
 */
@Log4j
public class SkillEnableEventHandler extends EventHandler {
    /**
     * {@inheritDoc}
     */
    @Override
    public String getEventRequestName() {
        return "AlexaSkillEvent.SkillEnabled";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canHandle(HandlerInput handlerInput) {
        return handlerInput.matches(Predicates.requestType(SkillEnabledRequest.class));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Response> handle() {
        Optional.of(handlerInput)
                .map(HandlerInput::getRequestEnvelope)
                .map(RequestEnvelope::getContext)
                .map(Context::getSystem)
                .map(SystemState::getUser)
                .map(User::getUserId).ifPresent((id) -> {
            log.info(String.format("User %s is enabling the skill, saving record from DynamoDB", id));
            obaDao.saveUserEnableData(new ObaUserEnableItem(id, Instant.now().getEpochSecond()));
        });
        return Optional.empty();
    }
}
