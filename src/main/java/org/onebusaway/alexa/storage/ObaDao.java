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
package org.onebusaway.alexa.storage;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;

/**
 * OneBusAway data access object to retrieve or save userData.
 */
public class ObaDao {
    private final ObaDynamoDbClient dynamoDbClient;

    public ObaDao(ObaDynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    /**
     * Reads and returns the user profile info using user information from the
     * session.
     *
     * @param userId
     * @return the user profile info using user information from the session
     */
    public Optional<ObaUserDataItem> getUserData(String userId) {
        ObaUserDataItem dataItem = new ObaUserDataItem();

        if (StringUtils.isBlank(userId)) {
            return Optional.empty();
        }
        dataItem.setUserId(userId);

        Optional<ObaUserDataItem> item = dynamoDbClient.loadItem(dataItem);
        if (item.isPresent() && item.get().getStopId() == null) {
            throw new RuntimeException(String.format("User %s does not have a stop ID set"));
        }

        return item;
    }

    public List<ObaUserRelationItem> getObaUserRelations(String userId) {
        ObaUserRelationItem obaUserRelationItem = new ObaUserRelationItem();
        obaUserRelationItem.setUserId(userId);
        return dynamoDbClient.loadItems(obaUserRelationItem);
    }

    /**
     * Saves the user profile info into the database.
     *
     * @param user
     */
    public void saveUserData(ObaUserDataItem user) {
        dynamoDbClient.saveItem(user);
    }

    /**
     * Save personId userId relationship to the table.
     */
    public void saveUserRelationData(ObaUserRelationItem obaUserRelationItem) {
        dynamoDbClient.saveItem(obaUserRelationItem);
    }

    public void saveUserEnableData(ObaUserEnableItem obaUserEnableItem) {
        dynamoDbClient.saveItem(obaUserEnableItem);
    }

    /**
     * Removing list of obaUserData items from database.
     *
     * @param obaUserRelationItems
     */
    public void removeAllUserDataItem(List<ObaUserDataItem> obaUserRelationItems) {
        dynamoDbClient.removeObaDataItems(obaUserRelationItems);
    }

    /**
     * Removing list of obaUserRelation items from database.
     *
     * @param obaUserRelationItems
     */
    public void removeAllUserRelationData(List<ObaUserRelationItem> obaUserRelationItems) {
        dynamoDbClient.removeObaRelations(obaUserRelationItems);
    }
}
