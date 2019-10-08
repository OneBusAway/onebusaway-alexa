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

import com.amazon.ask.model.Intent;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.Slot;
import com.amazon.ask.request.exception.handler.impl.AbstractHandlerInput;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;
import org.onebusaway.alexa.constant.SessionAttribute;
import org.onebusaway.alexa.lib.ObaUserClient;
import org.onebusaway.alexa.storage.ObaUserDataItem;
import org.onebusaway.alexa.util.CityUtil;
import org.onebusaway.io.client.elements.ObaRegion;
import org.onebusaway.location.Location;

import java.io.IOException;
import java.util.Optional;

import static org.onebusaway.alexa.constant.Prompt.ASK_FOR_STOP;
import static org.onebusaway.alexa.constant.Prompt.FOUND_CITY;
import static org.onebusaway.alexa.constant.SessionAttribute.CITY_NAME;
import static org.onebusaway.alexa.constant.SessionAttribute.EXPERIMENTAL_REGIONS;
import static org.onebusaway.alexa.constant.SessionAttribute.OBA_BASE_URL;
import static org.onebusaway.alexa.constant.SessionAttribute.REGION_ID;
import static org.onebusaway.alexa.constant.SessionAttribute.REGION_NAME;

/**
 * Handler for SetCity intent request (e.g. "ask OneBusAway set my region").
 */
@Log4j
public class SetCityIntentHandler extends IntentHandler {
    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public String getIntentRequestName() {
        return "SetCityIntent";
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public Optional<Response> handleWithObaData(final ObaUserDataItem obaUserDataItem, final ObaUserClient obaUserClient) {
        return handleWithoutObaData();
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public Optional<Response> handleWithoutObaData() {
        final String cityName = getCityNameFromRequest();
        log.debug("Into City:" + cityName);
        if (StringUtils.isBlank(cityName)) {
            log.debug("City is empty");
            return CityUtil.askForCityResponse();
        }

        boolean experimentalRegions = getSessionAttribute(EXPERIMENTAL_REGIONS, Boolean.class, false);
        Optional<Location> location = googleMaps.geocode(cityName);

        //location can be null if there is an exception from Google GeocodingApi.
        if (!location.isPresent()) {
            log.debug("Location is not present.");
            return CityUtil.askForCityResponse();
        }

        try {
            Optional<ObaRegion> region = obaClient.getClosestRegion(location.get(), experimentalRegions);
            boolean isRegionNotExist = !region.map(ObaRegion::getObaBaseUrl).isPresent();
            if (isRegionNotExist) {
                return CityUtil.askForCityResponse(cityName, this.attributesManager, this.obaClient);
            }

            updateSessionWithCityInfo(cityName, region.get());

            if (askState == SessionAttribute.AskState.STOP_BEFORE_CITY) {
                return fulfillCityAndStop();
            }
            return promptHelper.getResponse(promptHelper.getPrompt(FOUND_CITY, region.get().getName()), promptHelper.getPrompt(ASK_FOR_STOP));
        } catch (IOException e) {
            log.error(e);
            return CityUtil.askForCityResponse(cityName, this.attributesManager, this.obaClient);
        }
    }

    /**
     * Helper method to get the city name from Alexa request.
     *
     * @return
     */
    private String getCityNameFromRequest() {
        return Optional.of(handlerInput)
                .map(AbstractHandlerInput::getRequest)
                .map(r -> (IntentRequest) r)
                .map(IntentRequest::getIntent)
                .map(Intent::getSlots)
                .map(m -> m.get(CITY_NAME))
                .map(Slot::getValue)
                .orElse(null);
    }

    /**
     * Helper method to update the Alexa session with city related attributes.
     *
     * @param cityName
     * @param obaRegion
     */
    private void updateSessionWithCityInfo(String cityName, ObaRegion obaRegion) {
        addOrUpdateSessionAttribute(CITY_NAME, cityName);
        addOrUpdateSessionAttribute(REGION_ID, obaRegion.getId());
        addOrUpdateSessionAttribute(REGION_NAME, obaRegion.getName());
        addOrUpdateSessionAttribute(OBA_BASE_URL, obaRegion.getObaBaseUrl());
    }
}
