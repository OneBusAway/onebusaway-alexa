package org.onebusaway.alexa.storage;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBVersionAttribute;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Getter
    @Setter
    @DynamoDBAttribute(attributeName = "RegionId")
    private long regionId;

    @Getter
    @Setter
    @DynamoDBAttribute(attributeName = "RegionName")
    private String regionName;

    /**
     * OBA Base URL is cached so we don't need to hit the Regions API before getting arrivals.  This should be
     * occasionally refreshed, although it probably won't change very often.
     */
    @Getter
    @Setter
    @DynamoDBAttribute(attributeName = "ObaBaseUrl")
    private String obaBaseUrl;

    @DynamoDBVersionAttribute
    private Long version;
}