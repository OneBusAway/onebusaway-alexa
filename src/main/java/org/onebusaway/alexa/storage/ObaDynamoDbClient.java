/*
 * Copyright 2016 Sean J. Barbeau (sjbarbeau@gmail.com),
 * Philip M. White (philip@mailworks.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.alexa.storage;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;

import java.util.List;
import java.util.Optional;

/**
 * Client for DynamoDB persistance layer for the OneBusAway skill.
 */
public class ObaDynamoDbClient {
    private final AmazonDynamoDBClient dynamoDBClient;

    public ObaDynamoDbClient(AWSCredentials creds) {
        dynamoDBClient = new AmazonDynamoDBClient(creds);
    }

    /**
     * Loads an item from DynamoDB by primary Hash Key. Callers of this method
     * should pass in an object which represents an item in the DynamoDB table
     * item with the primary key populated.
     *
     * @param tableItem
     * @return
     */
    public Optional<ObaUserDataItem> loadItem(final ObaUserDataItem tableItem) {
        DynamoDBMapper mapper = createDynamoDBMapper();
        ObaUserDataItem item = mapper.load(tableItem);
        return Optional.ofNullable(item);
    }

    /**
     * @param obaUserRelationItem
     * @return
     */
    public List<ObaUserRelationItem> loadItems(final ObaUserRelationItem obaUserRelationItem) {
        DynamoDBMapper mapper = createDynamoDBMapper();
        DynamoDBQueryExpression<ObaUserRelationItem> queryExpression = new DynamoDBQueryExpression().withHashKeyValues(obaUserRelationItem);
        return mapper.query(ObaUserRelationItem.class, queryExpression);
    }

    /**
     * Stores an item to DynamoDB.
     *
     * @param tableItem
     */
    public void saveItem(final ObaUserDataItem tableItem) {
        DynamoDBMapper mapper = createDynamoDBMapper();
        mapper.save(tableItem);
    }

    /**
     * Store ObaUserRelation item to DynamoDB.
     *
     * @param obaUserRelationItem
     */
    public void saveItem(final ObaUserRelationItem obaUserRelationItem) {
        DynamoDBMapper mapper = createDynamoDBMapper();
        mapper.save(obaUserRelationItem);
    }

    /**
     * Store obaUserEnable item to DynamoDB.
     *
     * @param obaUserEnableItem
     */
    public void saveItem(final ObaUserEnableItem obaUserEnableItem) {
        DynamoDBMapper mapper = createDynamoDBMapper();
        mapper.save(obaUserEnableItem);
    }

    /**
     * Remove ObaUserDataItems from DynamoDB.
     *
     * @param obaUserDataItems
     */
    public void removeObaDataItems(final List<ObaUserDataItem> obaUserDataItems) {
        DynamoDBMapper mapper = createDynamoDBMapper();
        mapper.batchDelete(obaUserDataItems);
    }

    /**
     * Remove ObaUserRelation items from DynamoDB.
     *
     * @param obaUserRelationItems
     */
    public void removeObaRelations(final List<ObaUserRelationItem> obaUserRelationItems) {
        DynamoDBMapper mapper = createDynamoDBMapper();
        mapper.batchDelete(obaUserRelationItems);
    }

    /**
     * Creates a {@link DynamoDBMapper} using the default configurations.
     *
     * @return
     */
    private DynamoDBMapper createDynamoDBMapper() {
        return new DynamoDBMapper(dynamoDBClient);
    }
}
