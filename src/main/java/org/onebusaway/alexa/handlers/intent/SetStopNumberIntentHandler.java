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

import com.amazon.ask.model.Intent;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.Slot;
import com.amazon.ask.request.exception.handler.impl.AbstractHandlerInput;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;
import org.onebusaway.alexa.lib.ObaUserClient;
import org.onebusaway.alexa.storage.ObaUserDataItem;

import java.util.Optional;

import static org.onebusaway.alexa.constant.Prompt.ASK_FOR_STOP;
import static org.onebusaway.alexa.constant.Prompt.LOOKING_FOR_STOP_NUMBER;
import static org.onebusaway.alexa.constant.SessionAttribute.STOP_ID;

/**
 * Handler for SetStopNumber intent request (e.g. "ask OneBusAway change my stop").
 */
@Log4j
public class SetStopNumberIntentHandler extends IntentHandler {
    /**
     * {@inheritDoc}
     */
    @Override
    public String getIntentRequestName() {
        return "SetStopNumberIntent";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Response> handleWithObaData(ObaUserDataItem obaUserDataItem, ObaUserClient obaUserClient) {
        return handleWithoutObaData();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Response> handleWithoutObaData() {
        final String stopId = getStopNumberFromRequest();
        if (StringUtils.isBlank(stopId)) {
            log.info("Stop id not found");
            return promptHelper.getResponse(LOOKING_FOR_STOP_NUMBER, ASK_FOR_STOP);
        }
        log.info("Stop id is " + stopId);
        addOrUpdateSessionAttribute(STOP_ID, stopId);
        return fulfillCityAndStop();
    }

    /**
     * {@inheritDoc}
     */
    private String getStopNumberFromRequest() {
        return Optional.of(handlerInput)
                .map(AbstractHandlerInput::getRequest)
                .map(request -> (IntentRequest) request)
                .map(IntentRequest::getIntent)
                .map(Intent::getSlots).map(m -> m.get(STOP_ID))
                .map(Slot::getValue).orElse(null);
    }
}
