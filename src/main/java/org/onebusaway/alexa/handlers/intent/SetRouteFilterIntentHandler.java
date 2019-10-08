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
import lombok.extern.log4j.Log4j;
import org.onebusaway.alexa.exception.OneBusAwayException;
import org.onebusaway.alexa.lib.ObaUserClient;
import org.onebusaway.alexa.storage.ObaUserDataItem;
import org.onebusaway.alexa.util.CityUtil;
import org.onebusaway.alexa.util.RouteFilterUtil;
import org.onebusaway.alexa.util.StorageUtil;
import org.onebusaway.io.client.elements.ObaRoute;
import org.onebusaway.io.client.request.ObaStopResponse;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.onebusaway.alexa.constant.Prompt.COMMUNICATION_ERROR_MESSAGE;
import static org.onebusaway.alexa.constant.Prompt.ONLY_ONE_ROUTE;
import static org.onebusaway.alexa.constant.SessionAttribute.DIALOG_ROUTES_TO_FILTER;
import static org.onebusaway.alexa.constant.SessionAttribute.REGION_NAME;
import static org.onebusaway.alexa.constant.SessionAttribute.STOP_CODE;
import static org.onebusaway.alexa.constant.SessionAttribute.STOP_ID;

/**
 * Handler for SetRouteFilter intent request(e.g. "ask OneBusAway set a route filter").
 */
@Log4j
public class SetRouteFilterIntentHandler extends IntentHandler {
    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public String getIntentRequestName() {
        return "SetRouteFilter";
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public Optional<Response> handleWithObaData(final ObaUserDataItem obaUserDataItem, final ObaUserClient obaUserClient) {
        // Make sure we clear any existing routes to filter for this session (but leave any persisted)
        addOrUpdateSessionAttribute(DIALOG_ROUTES_TO_FILTER, null);

        final String stopId = getSessionAttribute(STOP_ID, String.class);
        final String regionName = getSessionAttribute(REGION_NAME, String.class);

        try {
            final ObaStopResponse response = obaUserClient.getStop(stopId);
            final List<ObaRoute> routes = response.getRoutes();
            if (routes.size() <= 1) {
                final String speech = promptHelper.getPrompt(ONLY_ONE_ROUTE, Integer.toString(response.getCode()));
                StorageUtil.saveOutputForRepeat(speech, obaDao, obaUserDataItem);
                return promptHelper.getResponse(speech);
            }

            addOrUpdateSessionAttribute(STOP_CODE, response.getStopCode());
            return RouteFilterUtil.askUserAboutFilterRoute(attributesManager, routes);
        } catch (IOException e) {
            log.error("Error getting details for stop + " + stopId + " in region " + regionName + ": " + e.getMessage());
            throw new OneBusAwayException(promptHelper.getPrompt(COMMUNICATION_ERROR_MESSAGE), e);
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
