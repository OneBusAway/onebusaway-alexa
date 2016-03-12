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

import com.amazon.speech.speechlet.Session;

import java.util.Optional;

public class ObaDao {
	private final ObaDynamoDbClient dynamoDbClient;

	public ObaDao(ObaDynamoDbClient dynamoDbClient) {
		this.dynamoDbClient = dynamoDbClient;
	}

	/**
	 * Reads and returns the user profile info using user information from the
	 * session.
	 *
	 * @param session
	 * @return the user profile info using user information from the session
	 */
	public Optional<ObaUserDataItem> getUserData(Session session) {
		ObaUserDataItem hashKey = new ObaUserDataItem();
		String userId = session.getUser().getUserId();

		if (userId == null) {
			return Optional.empty();
		}
		else {
			hashKey.setUserId(userId);

			Optional<ObaUserDataItem> item = dynamoDbClient.loadItem(hashKey);
			if (item.isPresent() && item.get().getStopId() == null) {
				throw new RuntimeException(String.format("User %s does not have a stop ID set"));
			}

			return item;
		}
	}

	/**
	 * Saves the user profile info into the database.
	 * 
	 * @param user
	 */
	public void saveUserData(ObaUserDataItem user) {
		dynamoDbClient.saveItem(user);
	}
}
