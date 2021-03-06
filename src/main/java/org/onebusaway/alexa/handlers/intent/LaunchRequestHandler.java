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

package org.onebusaway.alexa.handlers.intent;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.LaunchRequest;
import com.amazon.ask.model.Request;
import com.amazon.ask.model.Response;
import com.amazon.ask.request.Predicates;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;
import org.onebusaway.alexa.constant.SessionAttribute;
import org.onebusaway.alexa.exception.OneBusAwayException;
import org.onebusaway.alexa.lib.ObaUserClient;
import org.onebusaway.alexa.storage.ObaUserDataItem;
import org.onebusaway.alexa.util.CityUtil;
import org.onebusaway.io.client.request.ObaStopResponse;

import java.util.Optional;

import static org.onebusaway.alexa.constant.Prompt.ASK_FOR_CITY;
import static org.onebusaway.alexa.constant.Prompt.ASK_FOR_STOP;
import static org.onebusaway.alexa.constant.Prompt.COMMUNICATION_ERROR_MESSAGE;
import static org.onebusaway.alexa.constant.Prompt.COPY_CONFIRMATION;
import static org.onebusaway.alexa.constant.Prompt.COPY_PROFILE;
import static org.onebusaway.alexa.constant.Prompt.REASK_FOR_STOP;
import static org.onebusaway.alexa.constant.Prompt.WELCOME_MESSAGE;
import static org.onebusaway.alexa.constant.SessionAttribute.ASK_STATE;

/**
 * Handler for Launch request (e.g. "open OneBusAway").
 */
@Log4j
public class LaunchRequestHandler extends IntentHandler {
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canHandle(final HandlerInput handlerInput) {
        // Alexa Launch request has to be matched by type instead of request name. Override the canHandle logic from super class.
        boolean canHandleRequest = handlerInput.matches(Predicates.requestType(LaunchRequest.class));
        final String requestType = Optional.ofNullable(handlerInput).map(HandlerInput::getRequest).map(Request::getType).orElse("Launch");
        log.info(String.format("%s handler canHandle returns %s for %s", this.getClass().getSimpleName(), canHandleRequest, requestType));
        return canHandleRequest;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getIntentRequestName() {
        return "LaunchRequest";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Response> handleWithObaData(final ObaUserDataItem obaUserDataItem, final ObaUserClient obaUserClient) {
        return CityUtil.tellArrivals(obaUserDataItem, obaUserClient, attributesManager, obaDao);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Response> handleWithoutObaData() {
        if (this.personalization.isPersonalized()) {
            Optional<ObaUserDataItem> obaUserDataItem = this.obaDao.getUserData(personalization.getUserId());
            if (obaUserDataItem.isPresent()) {
                try {
                    ObaUserClient obaUserClient = new ObaUserClient(obaUserDataItem.get().getObaBaseUrl());
                    final ObaStopResponse stop = obaUserClient.getStopDetails(obaUserDataItem.get().getStopId());
                    addOrUpdateSessionAttribute(ASK_STATE, SessionAttribute.AskState.COPY_PROFILE_CONFIRM.toString());
                    final String speech =
                            promptHelper.getPrompt(COPY_PROFILE, obaUserDataItem.get().getCity(), stop.getStopCode());
                    final String reprompt = promptHelper.getPrompt(COPY_CONFIRMATION);
                    return promptHelper.getResponse(speech, reprompt);
                } catch (Exception e) {
                    log.error(e);
                    throw new OneBusAwayException(promptHelper.getPrompt(COMMUNICATION_ERROR_MESSAGE));
                }
            }
        }
        if (StringUtils.isBlank(getSessionAttribute(SessionAttribute.CITY_NAME, String.class))) {
            return promptHelper.getResponse(WELCOME_MESSAGE, ASK_FOR_CITY);
        } else {
            return promptHelper.getResponse(REASK_FOR_STOP, ASK_FOR_STOP);
        }
    }
}
