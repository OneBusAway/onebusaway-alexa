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
import org.onebusaway.alexa.lib.ObaUserClient;
import org.onebusaway.alexa.storage.ObaUserDataItem;
import org.onebusaway.alexa.util.CityUtil;

import java.util.Optional;

import static org.onebusaway.alexa.constant.Prompt.HELP_MESSAGE;

/**
 * Handler for Help intent request (e.g. "ask OneBusAway help").
 */
public class HelpIntentHandler extends IntentHandler {
    /**
     * {@inheritDoc}
     */
    @Override
    public String getIntentRequestName() {
        return "AMAZON.HelpIntent";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Response> handleWithObaData(final ObaUserDataItem obaUserDataItem, final ObaUserClient obaUserClient) {
        return promptHelper.getResponse(HELP_MESSAGE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Response> handleWithoutObaData() {
        return CityUtil.askForCityResponse();
    }
}
