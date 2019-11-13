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

package org.onebusaway.alexa.personalization;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Context;
import com.amazon.ask.model.Person;
import com.amazon.ask.model.RequestEnvelope;
import com.amazon.ask.model.interfaces.system.SystemState;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;
import org.onebusaway.alexa.config.PromptsConfig;
import org.onebusaway.alexa.exception.OneBusAwayException;
import org.onebusaway.alexa.helper.PromptHelper;
import org.onebusaway.alexa.util.SessionUtil;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Optional;

import static org.onebusaway.alexa.constant.Prompt.GENERAL_ERROR_MESSAGE;
import static org.onebusaway.alexa.constant.SessionAttribute.PRINCIPLE_ID;

/**
 * Utilities to extract personalization information from Alexa Request, principle Id will be
 * personId when it is available, otherwise userId.
 */
@Log4j
public class PersonalizationInfoExtractor {
    private static PromptHelper promptHelper =
            new AnnotationConfigApplicationContext(PromptsConfig.class).getBean("promptHelper", PromptHelper.class);

    private static final String PERSONID_PREFIX = "amzn1.ask.person";

    /**
     * Extracts the personId and userId from the session or request.
     *
     * @param handlerInput
     * @return PersonalizationInfo instance that contains personalized information
     * @see PersonalizationInfo
     */
    public static PersonalizationInfo extractPersonalizationStatusFromAlexaRequest(HandlerInput handlerInput) {
        if (handlerInput == null) {
            throw new OneBusAwayException(promptHelper.getPrompt(GENERAL_ERROR_MESSAGE));
        }
        final String userId = handlerInput.getRequestEnvelope().getSession().getUser().getUserId();
        final String principleId = SessionUtil.getSessionAttribute(handlerInput.getAttributesManager(), PRINCIPLE_ID, String.class);
        if (StringUtils.isNotBlank(principleId)) {
            return new PersonalizationInfo(principleId, userId, principleId, principleId.startsWith(PERSONID_PREFIX));
        }

        return Optional.of(handlerInput)
                .map(HandlerInput::getRequestEnvelope)
                .map(RequestEnvelope::getContext)
                .map(Context::getSystem)
                .map(SystemState::getPerson)
                .map(Person::getPersonId)
                .filter(StringUtils::isNotBlank)
                .map((pid) -> {
                    log.info("Person ID is " + pid);
                    return new PersonalizationInfo(pid, userId, pid, true);
                }).orElseGet(() -> {
                    log.info("Person ID does not exist, using userId as principleId");
                    return new PersonalizationInfo(userId, userId, StringUtils.EMPTY, false);
                });
    }
}
