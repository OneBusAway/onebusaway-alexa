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
import org.onebusaway.alexa.constant.SessionAttribute.AskState;
import org.onebusaway.alexa.exception.OneBusAwayException;
import org.onebusaway.alexa.handlers.BaseHandler;
import org.onebusaway.alexa.helper.PromptHelper;
import org.onebusaway.alexa.lib.GoogleMaps;
import org.onebusaway.alexa.lib.ObaClient;
import org.onebusaway.alexa.lib.ObaUserClient;
import org.onebusaway.alexa.personalization.PersonalizationInfoExtractor;
import org.onebusaway.alexa.personalization.PersonalizationInfo;
import org.onebusaway.alexa.storage.ObaUserDataItem;
import org.onebusaway.alexa.storage.ObaUserRelationItem;
import org.onebusaway.alexa.util.CityUtil;
import org.onebusaway.alexa.util.SessionUtil;

import javax.inject.Inject;
import java.util.Optional;

import static org.onebusaway.alexa.constant.Prompt.GENERAL_ERROR_MESSAGE;
import static org.onebusaway.alexa.constant.SessionAttribute.ASK_STATE;
import static org.onebusaway.alexa.constant.SessionAttribute.AskState.NONE;
import static org.onebusaway.alexa.constant.SessionAttribute.PRINCIPLE_ID;

/**
 * Superclass for all intents handlers,
 * IntentHandler is responsible to read the ObaUserDataItem from DynamoDB
 * and handle the intent request
 * with or without ObaUserDataItem.
 * When ObaUserDataItem exist, it means user have previously on-boarded
 * (setup the city name and city and records has been stored to DynamoDB)
 * otherwise user is on-boarding.
 */
@Log4j
abstract public class IntentHandler extends BaseHandler {
    @Inject
    protected GoogleMaps googleMaps;

    @Inject
    protected ObaClient obaClient;

    @Inject
    protected PromptHelper promptHelper;

    protected AskState askState;

    protected PersonalizationInfo personalization;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRequestName() {
        return getIntentRequestName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Response> handle() {
        personalizationInitialization();
        final Optional<ObaUserDataItem> obaUserDataItem = obaDao.getUserData(personalization.getPrincipleId());
        populateAlexaSessionAttributes(obaUserDataItem);
        this.askState = AskState.valueOf(getSessionAttribute(ASK_STATE, String.class, NONE.toString()));
        log.info(String.format("askState is %s when request start", askState));
        addOrUpdateSessionAttribute(ASK_STATE, NONE.toString());

        if (obaUserDataItem.isPresent()) {
            try {
                final ObaUserClient obaUserClient = new ObaUserClient(obaUserDataItem.get().getObaBaseUrl());
                return handleWithObaData(obaUserDataItem.get(), obaUserClient);
            } catch (Exception e) {
                throw new OneBusAwayException(promptHelper.getPrompt(GENERAL_ERROR_MESSAGE), e);
            }
        } else {
            return handleWithoutObaData();
        }
    }

    /**
     * Initialized skill personalization.
     */
    private void personalizationInitialization() {
        this.personalization = PersonalizationInfoExtractor.extractPersonalizationStatusFromAlexaRequest(handlerInput);
        addOrUpdateSessionAttribute(PRINCIPLE_ID, personalization.getPrincipleId());
        promptHelper.setPersonalizationStatus(personalization);
        log.info("Personalized experience is: " + personalization);

        if (personalization.isPersonalized()) {
            log.info("Saving userId personId relation to DB");
            new Thread(() -> {
                try {
                    obaDao.saveUserRelationData(new ObaUserRelationItem(personalization.getUserId(), personalization.getPersonId()));
                } catch (Exception e) {
                    log.error(String.format("Failed to save relation for %s %s.", personalization.getUserId(), personalization.getPersonId()), e);
                }
            }).start();
        }
    }

    /**
     * Retrieve the ObaUserDataItem from DynamoDB.
     *
     * @return
     */
    private Optional<ObaUserDataItem> retrieveObaData() {
        Optional<ObaUserDataItem> obaUserDataItem = Optional.empty();
        if (personalization.isPersonalized()) {
            log.info("Retrieving person level ObaData.");
            obaUserDataItem = obaDao.getUserData(personalization.getPersonId());
        } else {
            log.info("Retrieving user level ObaData.");
            obaUserDataItem = obaDao.getUserData(personalization.getUserId());
        }
        return obaUserDataItem;
    }

    /**
     * Populates Alexa skill session with Oba user data.
     *
     * @param obaUserDataItem OneBusAway User Data item
     */
    private void populateAlexaSessionAttributes(final Optional<ObaUserDataItem> obaUserDataItem) {
        SessionUtil.populateAttributes(attributesManager, obaUserDataItem);
    }

    /**
     * Helper method to fulfill city and stop number to on-board user.
     *
     * @return alexa response based on cityName and stopId
     */
    protected Optional<Response> fulfillCityAndStop() {
        final String userId = personalization.getPrincipleId();
        return CityUtil.fulfillCityAndStop(
                userId, this.attributesManager, this.googleMaps, this.obaClient, this.obaDao);
    }

    /**
     * Helper method to add or update session attribute.
     *
     * @param key key name in the session
     * @param value value associated with the key
     */
    protected void addOrUpdateSessionAttribute(String key, Object value) {
        SessionUtil.addOrUpdateSessionAttribute(this.attributesManager, key, value);
    }

    /**
     * Helper method to get the session attribute.
     *
     * @param key key name in the session
     * @param clazz session attribute type
     * @return alexa session attribute
     */
    protected <T> T getSessionAttribute(String key, Class<T> clazz) {
        return SessionUtil.getSessionAttribute(this.attributesManager, key, clazz);
    }

    /**
     * Helper method to get the session attribute, it will return default value when the session attribute is not exist.
     *
     * @param key key name in the session
     * @param clazz session attribute type
     * @param defaultValue default value if session value doesn't exist
     * @return alexa session attribute, will return default value if session attribute is none
     */
    protected <T> T getSessionAttribute(String key, Class<T> clazz, T defaultValue) {
        return SessionUtil.getSessionAttribute(this.attributesManager, key, clazz, defaultValue);
    }

    /**
     * Gets the intent request name.
     *
     * @return intent request name
     */
    abstract public String getIntentRequestName();

    /**
     * Handle the request when ObaUserDataItem available.
     *
     * @param obaUserDataItem OneBusAway User Data item
     * @param obaUserClient client used to access the OBA REST API for a local OBA server
     * @return alexa response
     */
    abstract public Optional<Response> handleWithObaData(final ObaUserDataItem obaUserDataItem, final ObaUserClient obaUserClient);

    /**
     * Handle the request without ObaUserDataItem.
     *
     * @return alexa response
     */
    abstract public Optional<Response> handleWithoutObaData();
}