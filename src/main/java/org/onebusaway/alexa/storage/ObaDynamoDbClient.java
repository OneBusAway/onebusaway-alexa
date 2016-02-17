/*
 * Copyright (C) 2015 Sean J. Barbeau (sjbarbeau@gmail.com).
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
	 * Stores an item to DynamoDB.
	 * 
	 * @param tableItem
	 */
	public void saveItem(final ObaUserDataItem tableItem) {
		DynamoDBMapper mapper = createDynamoDBMapper();
		mapper.save(tableItem);
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
