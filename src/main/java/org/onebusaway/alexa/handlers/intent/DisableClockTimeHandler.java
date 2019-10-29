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

import java.io.IOException;
import java.util.Optional;
import java.util.TimeZone;

import static org.onebusaway.alexa.constant.Prompt.COMMUNICATION_ERROR_MESSAGE;
import static org.onebusaway.alexa.constant.Prompt.DISABLE_CLOCK_TIME;
import static org.onebusaway.alexa.constant.SessionAttribute.CLOCK_TIME;
import static org.onebusaway.alexa.constant.SessionAttribute.TIME_ZONE;

/**
 * Handler for DisableClockTime intent request, (e.g. "ask OneBusAway disable clock time").
 */
@Log4j
public class DisableClockTimeHandler extends IntentHandler {
    /**
     * {@inheritDoc}
     */
    @Override
    public String getIntentRequestName() {
        return "DisableClockTime";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Response> handleWithObaData(final ObaUserDataItem obaUserDataItem, final ObaUserClient obaUserClient) {
        try {
            final TimeZone timeZone = obaUserClient.getTimeZone();
            // Update DAO
            obaUserDataItem.setSpeakClockTime(0);
            obaUserDataItem.setTimeZone(timeZone.getID());
            obaDao.saveUserData(obaUserDataItem);

            // Update session
            addOrUpdateSessionAttribute(CLOCK_TIME, 0);
            addOrUpdateSessionAttribute(TIME_ZONE, timeZone.getID());
            final String output = promptHelper.getPrompt(DISABLE_CLOCK_TIME);
            StorageUtil.saveOutputForRepeat(output, obaDao, obaUserDataItem);
            return promptHelper.getResponse(output);
        } catch (IOException e) {
            log.error(e);
            throw new OneBusAwayException(promptHelper.getPrompt(COMMUNICATION_ERROR_MESSAGE), e);
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
