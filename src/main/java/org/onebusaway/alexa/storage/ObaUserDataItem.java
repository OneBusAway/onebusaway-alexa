package org.onebusaway.alexa.storage;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Optional;

/**
 * Model representing an item of the User Data table in DynamoDB for the
 * OneBusAway skill.
 */
@DynamoDBTable(tableName = "ObaUserData")
@NoArgsConstructor
@AllArgsConstructor
public class ObaUserDataItem {
    @Getter
    @Setter
    @DynamoDBHashKey(attributeName = "UserId")
    private String userId;

    @Getter
    @Setter
    @DynamoDBAttribute(attributeName = "City")
    private String city;

    @Getter
    @Setter
    @DynamoDBAttribute(attributeName = "StopId")
    private String stopId;

    @DynamoDBVersionAttribute
    private Long version;
}