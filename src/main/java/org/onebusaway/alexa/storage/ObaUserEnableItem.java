package org.onebusaway.alexa.storage;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Model representing an item of the User Enable table in DynamoDB for the
 * OneBusAway skill.
 */
@DynamoDBTable(tableName = "ObaUserEnable")
@NoArgsConstructor
@AllArgsConstructor
public class ObaUserEnableItem {
    @Getter
    @Setter
    @DynamoDBHashKey(attributeName = "UserId")
    private String userId;

    @Getter
    @Setter
    @DynamoDBRangeKey(attributeName = "EnableTime")
    private long enableTime;
}
