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
import org.apache.commons.lang3.StringUtils;
import org.onebusaway.alexa.constant.Prompt;
import org.onebusaway.alexa.constant.SessionAttribute;
import org.onebusaway.alexa.exception.OneBusAwayException;
import org.onebusaway.alexa.lib.ObaUserClient;
import org.onebusaway.alexa.storage.ObaUserDataItem;
import org.onebusaway.alexa.util.CityUtil;
import org.onebusaway.alexa.util.RouteFilterUtil;
import org.onebusaway.alexa.util.StopUtil;

import java.util.Optional;

import static org.onebusaway.alexa.constant.SessionAttribute.ASK_STATE;

/**
 * Handler for Yes intent request(e.g. user say "Yes" during the dialog).
 */
@Log4j
public class YesIntentHandler extends IntentHandler {
    /**
     * {@inheritDoc}
     */
    @Override
    public String getIntentRequestName() {
        return "AMAZON.YesIntent";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Response> handleWithObaData(final ObaUserDataItem obaUserDataItem, final ObaUserClient obaUserClient) {
        if (askState == SessionAttribute.AskState.VERIFYSTOP) {
            addOrUpdateSessionAttribute(ASK_STATE, askState);
            return handleWithoutObaData();
        }

        if (askState == SessionAttribute.AskState.FILTER_INDIVIDUAL_ROUTE) {
            return RouteFilterUtil.handleFilterRouteResponse(this.attributesManager, true, obaDao, obaUserDataItem);
        }

        log.error("Received yes intent without a question.");
        return promptHelper.getResponse(Prompt.GENERAL_ERROR_MESSAGE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Response> handleWithoutObaData() {
        if (askState == SessionAttribute.AskState.VERIFYSTOP) {
            return StopUtil.handleDuplicateStopResponse(personalization.getPrincipleId(), attributesManager, true, googleMaps, obaClient, obaDao);
        }

        if (askState == SessionAttribute.AskState.COPY_PROFILE_CONFIRM && personalization.isPersonalized()) {
            try {
                Optional<ObaUserDataItem> obaUserDataItem = this.obaDao.getUserData(personalization.getUserId());
                obaUserDataItem.get().setUserId(personalization.getPersonId());
                obaUserDataItem.get().setPreviousResponse(StringUtils.EMPTY);
                ObaUserClient obaUserClient = new ObaUserClient(obaUserDataItem.get().getObaBaseUrl());
                this.obaDao.saveUserData(obaUserDataItem.get());
                populateAlexaSessionAttributes(obaUserDataItem);
                return CityUtil.tellArrivals(obaUserDataItem.get(), obaUserClient, attributesManager, obaDao);
            } catch (Exception e) {
                log.error(e);
                throw new OneBusAwayException(promptHelper.getPrompt(Prompt.COMMUNICATION_ERROR_MESSAGE), e);
            }
        }

        log.error("Received yes intent without a question.");
        return CityUtil.askForCityResponse();
    }
}
