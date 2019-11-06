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
import org.onebusaway.alexa.exception.OneBusAwayException;
import org.onebusaway.alexa.lib.ObaUserClient;
import org.onebusaway.alexa.storage.ObaUserDataItem;
import org.onebusaway.alexa.util.CityUtil;
import org.onebusaway.alexa.util.StorageUtil;
import org.onebusaway.io.client.request.ObaStopResponse;

import java.io.IOException;
import java.util.Optional;

import static org.onebusaway.alexa.constant.Prompt.COMMUNICATION_ERROR_MESSAGE;
import static org.onebusaway.alexa.constant.Prompt.GET_STOP;
import static org.onebusaway.alexa.helper.PromptHelper.ADDRESS_SSML_FORMAT;
import static org.onebusaway.alexa.util.SpeechUtil.replaceSpecialCharactersFromAddress;

/**
 * Handler for GetStopNumber intent request (e.g. "ask OneBusAway what is my stop").
 */
@Log4j
public class GetStopNumberIntentHandler extends IntentHandler {
    /**
     * {@inheritDoc}
     */
    @Override
    public String getIntentRequestName() {
        return "GetStopNumberIntent";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Response> handleWithObaData(final ObaUserDataItem obaUserDataItem, final ObaUserClient obaUserClient) {
        try {
            final ObaStopResponse stop = obaUserClient.getStopDetails(obaUserDataItem.getStopId());
            final String stopName = replaceSpecialCharactersFromAddress(stop.getName());
            final String speech =
                    promptHelper.getPrompt(GET_STOP, stop.getStopCode(), String.format(ADDRESS_SSML_FORMAT, stopName));
            StorageUtil.saveOutputForRepeat(speech, obaDao, obaUserDataItem);
            return promptHelper.getResponse(speech);
        } catch (IOException e) {
            log.error(e);
            throw new OneBusAwayException(promptHelper.getPrompt(COMMUNICATION_ERROR_MESSAGE));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Response> handleWithoutObaData() {
        return CityUtil.askForCityResponse();
    }
}
