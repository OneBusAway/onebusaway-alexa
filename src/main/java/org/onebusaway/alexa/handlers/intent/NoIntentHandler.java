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

import com.amazon.ask.model.Response;
import lombok.extern.log4j.Log4j;
import org.onebusaway.alexa.constant.SessionAttribute;
import org.onebusaway.alexa.lib.ObaUserClient;
import org.onebusaway.alexa.storage.ObaUserDataItem;
import org.onebusaway.alexa.util.CityUtil;
import org.onebusaway.alexa.util.RouteFilterUtil;
import org.onebusaway.alexa.util.StopUtil;

import java.util.Optional;

import static org.onebusaway.alexa.constant.Prompt.GENERAL_ERROR_MESSAGE;
import static org.onebusaway.alexa.constant.SessionAttribute.ASK_STATE;

/**
 * Handler for No Intent request (e.g. user say "Stop" during the dialog).
 */
@Log4j
public class NoIntentHandler extends IntentHandler {
    /**
     * {@inheritDoc}
     */
    @Override
    public String getIntentRequestName() {
        return "AMAZON.NoIntent";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Response> handleWithObaData(final ObaUserDataItem obaUserDataItem, final ObaUserClient obaUserClient) {
        if (askState == SessionAttribute.AskState.VERIFYSTOP) {
            // User confirmed that they want to select a particular stop - pass to anonSpeechlet to finish dialog
            // Restore the session ASK_STATE
            addOrUpdateSessionAttribute(ASK_STATE, askState);
            return handleWithoutObaData();
        }

        if (askState == SessionAttribute.AskState.FILTER_INDIVIDUAL_ROUTE) {
            return RouteFilterUtil.handleFilterRouteResponse(attributesManager, false, obaDao, obaUserDataItem);
        }

        log.error("Received no intent without a question.");
        return promptHelper.getResponse(GENERAL_ERROR_MESSAGE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Response> handleWithoutObaData() {
        if (askState == SessionAttribute.AskState.VERIFYSTOP) {
            String userId = handlerInput.getRequestEnvelope().getContext().getSystem().getUser().getUserId();
            return StopUtil.handleDuplicateStopResponse(userId, this.attributesManager, false, googleMaps, obaClient, obaDao);
        }
        log.error("Received no intent without a question.");
        return CityUtil.askForCityResponse();
    }
}
