package org.onebusaway.alexa.storage;

import com.amazon.speech.speechlet.Session;

public class ObaDao {
    private final ObaDynamoDbClient dynamoDbClient;

    public ObaDao(ObaDynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    /**
     * Reads and returns the user profile info using user information from the session.
     * <p>
     * Returns null if the item could not be found in the database.
     * 
     * @param session
     * @return the user profile info using user information from the session
     */
    public ObaUserDataItem getUserData(Session session) {
    	ObaUserDataItem item = new ObaUserDataItem();
        item.setUserId(session.getUser().getUserId());

        item = dynamoDbClient.loadItem(item);

        return item;
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
