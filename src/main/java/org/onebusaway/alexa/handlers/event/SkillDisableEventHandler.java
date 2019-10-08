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

package org.onebusaway.alexa.handlers.event;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Context;
import com.amazon.ask.model.RequestEnvelope;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.User;
import com.amazon.ask.model.events.skillevents.SkillDisabledRequest;
import com.amazon.ask.model.interfaces.system.SystemState;
import com.amazon.ask.request.Predicates;
import lombok.extern.log4j.Log4j;
import org.onebusaway.alexa.storage.ObaUserDataItem;
import org.onebusaway.alexa.storage.ObaUserRelationItem;
import org.springframework.util.CollectionUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Handler for AlexaSkillEvent.SkillDisabled event request.
 * (e.g. skill disable event will be sent to this handler when user disable OneBusAway skill)
 */
@Log4j
public class SkillDisableEventHandler extends EventHandler {
    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public String getEventRequestName() {
        return "AlexaSkillEvent.SkillDisabled";
    }

    /**
     * Override canHandle to decide whether the quest can be handled by request type.
     *
     * @param handlerInput
     * @return
     */
    @Override
    public boolean canHandle(HandlerInput handlerInput) {
        return handlerInput.matches(Predicates.requestType(SkillDisabledRequest.class));
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public Optional<Response> handle() {
        Optional.of(handlerInput)
                .map(HandlerInput::getRequestEnvelope)
                .map(RequestEnvelope::getContext)
                .map(Context::getSystem)
                .map(SystemState::getUser)
                .map(User::getUserId).
                ifPresent((id) -> {
                    log.info(String.format("User %s is disabling the skill, removing record from DynamoDB", id));
                    List<ObaUserRelationItem> obaUserRelationItemList = obaDao.getObaUserRelations(id);
                    if (CollectionUtils.isEmpty(obaUserRelationItemList)) {
                        //Initialize a new list to avoid NPE.
                        obaUserRelationItemList = new LinkedList();
                    }
                    //the list returned from DynamoDB is immutable so we need to create another list here.
                    List<ObaUserRelationItem> obaUserRelationItems = new LinkedList(obaUserRelationItemList);
                    //add current userId to remove the userId data.
                    obaUserRelationItems.add(new ObaUserRelationItem(id, id));
                    log.info("Removing ObaUserDataItems.");
                    obaDao.removeAllUserDataItem(obaUserRelationItems.stream().map((relation) -> {
                        ObaUserDataItem obaUserDataItem = new ObaUserDataItem();
                        obaUserDataItem.setUserId(relation.getPersonId());
                        return obaUserDataItem;
                    }).collect(Collectors.toList()));
                    log.info("Removing records from relation table.");
                    obaDao.removeAllUserRelationData(obaUserRelationItemList);
                });
        return Optional.empty();
    }
}
