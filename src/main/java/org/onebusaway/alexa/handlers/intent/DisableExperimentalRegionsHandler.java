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
import org.onebusaway.alexa.util.StorageUtil;

import java.io.IOException;
import java.util.Optional;

import static org.onebusaway.alexa.constant.Prompt.DISABLE_EXPERIMENTAL_REGIONS;
import static org.onebusaway.alexa.constant.SessionAttribute.EXPERIMENTAL_REGIONS;

/**
 * Handler for DisableExperimentalRegions intent request (e.g. "ask OneBusAway disable experimental region").
 */
public class DisableExperimentalRegionsHandler extends IntentHandler {
    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public String getIntentRequestName() {
        return "DisableExperimentalRegions";
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public Optional<Response> handleWithObaData(final ObaUserDataItem obaUserDataItem, final ObaUserClient obaUserClient) {
        // Update DAO
        obaUserDataItem.setExperimentalRegions(false);
        obaDao.saveUserData(obaUserDataItem);

        // Update session
        addOrUpdateSessionAttribute(EXPERIMENTAL_REGIONS, false);

        try {
            final String allRegions = CityUtil.allRegionsSpoken(obaUserClient.getAllRegions(false), false);
            final String speech = promptHelper.getPrompt(DISABLE_EXPERIMENTAL_REGIONS, allRegions);
            StorageUtil.saveOutputForRepeat(speech, obaDao, obaUserDataItem);
            return promptHelper.getResponse(speech);
        } catch (IOException e) {
            final String speech = promptHelper.getPrompt(DISABLE_EXPERIMENTAL_REGIONS, StringUtils.EMPTY);
            StorageUtil.saveOutputForRepeat(speech, obaDao, obaUserDataItem);
            return promptHelper.getResponse(speech);
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
