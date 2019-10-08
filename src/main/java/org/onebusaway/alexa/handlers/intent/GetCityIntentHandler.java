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
import org.onebusaway.alexa.util.StorageUtil;

import java.util.Optional;

import static org.onebusaway.alexa.constant.Prompt.ASK_FOR_CITY;
import static org.onebusaway.alexa.constant.Prompt.ASK_FOR_STOP;
import static org.onebusaway.alexa.constant.Prompt.GET_CITY;
import static org.onebusaway.alexa.constant.Prompt.GET_CITY_IN_SESSION_EMPTY;
import static org.onebusaway.alexa.constant.Prompt.GET_CITY_IN_SESSION_EXIST;
import static org.onebusaway.alexa.constant.SessionAttribute.CITY_NAME;

/**
 * Handler for GetCityIntent intent request (e.g. "ask OneBusAway where am I").
 */
public class GetCityIntentHandler extends IntentHandler {
    @Override
    public String getIntentRequestName() {
        return "GetCityIntent";
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public Optional<Response> handleWithObaData(final ObaUserDataItem obaUserDataItem, final ObaUserClient obaUserClient) {
        final String speech = promptHelper.getPrompt(GET_CITY, obaUserDataItem.getCity(), obaUserDataItem.getRegionName());
        StorageUtil.saveOutputForRepeat(speech, obaDao, obaUserDataItem);
        return promptHelper.getResponse(speech);
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public Optional<Response> handleWithoutObaData() {
        final String cityName = getSessionAttribute(CITY_NAME, String.class);
        if (StringUtils.isBlank(cityName)) {
            return promptHelper.getResponse(promptHelper.getPrompt(GET_CITY_IN_SESSION_EMPTY), promptHelper.getPrompt(ASK_FOR_CITY));
        } else {
            return promptHelper.getResponse(promptHelper.getPrompt(GET_CITY_IN_SESSION_EXIST, cityName), promptHelper.getPrompt(ASK_FOR_STOP));
        }
    }
}
