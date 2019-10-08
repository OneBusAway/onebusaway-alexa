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

package org.onebusaway.alexa.handlers.intent;

import com.amazon.ask.model.Response;
import org.apache.commons.lang3.StringUtils;
import org.onebusaway.alexa.lib.ObaUserClient;
import org.onebusaway.alexa.storage.ObaUserDataItem;
import org.onebusaway.alexa.util.CityUtil;

import java.io.IOException;
import java.util.Optional;

import static org.onebusaway.alexa.constant.Prompt.ENABLE_EXPERIMENTAL_REGIONS;
import static org.onebusaway.alexa.constant.SessionAttribute.EXPERIMENTAL_REGIONS;

/**
 * Handler for EnableExperimentalRegions intent request (e.g. "ask OneBusAway enable experimental region").
 */
public class EnableExperimentalRegionsHandler extends IntentHandler {
    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public String getIntentRequestName() {
        return "EnableExperimentalRegions";
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public Optional<Response> handleWithObaData(final ObaUserDataItem obaUserDataItem, final ObaUserClient obaUserClient) {
        // Update DAO
        obaUserDataItem.setExperimentalRegions(true);
        obaDao.saveUserData(obaUserDataItem);

        // Update session
        addOrUpdateSessionAttribute(EXPERIMENTAL_REGIONS, true);

        try {
            String allRegions = CityUtil.allRegionsSpoken(obaUserClient.getAllRegions(true), true);
            return promptHelper.getResponse(promptHelper.getPrompt(ENABLE_EXPERIMENTAL_REGIONS, allRegions));
        } catch (IOException e) {
            return promptHelper.getResponse(promptHelper.getPrompt(ENABLE_EXPERIMENTAL_REGIONS, StringUtils.EMPTY));
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public Optional<Response> handleWithoutObaData() {
        return CityUtil.askForCityResponse();
    }
}
