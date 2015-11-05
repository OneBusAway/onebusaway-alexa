package org.onebusaway.alexa.storage;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

/**
 * Model representing an item of the User Data table in DynamoDB for the OneBusAway
 * skill.
 * 
 * @author barbeau
 */
@DynamoDBTable(tableName="ObaUserData")
public class ObaUserDataItem {
	
    private String userId;
    private String city;
    private int regionId;
    private String obaBaseUrl;
    private String currentStopId;
    private String currentStopCode;
    /**
     * Comma-delimited set of routeIds that SHOULD be spoken to the user
     */
    private String routeFilter;

    @DynamoDBHashKey(attributeName="UserId")  
    public String getUserId() { return userId;}
    public void setUserId(String userId) {this.userId = userId;}
    
    @DynamoDBAttribute(attributeName="City")  
    public String getCity() {return city; }
    public void setCity(String city) { this.city = city; }
    
    @DynamoDBAttribute(attributeName="RegionId")  
    public int getRegionId() { return regionId; }
    public void setISBN(int regionId) { this.regionId = regionId; }
    
    @DynamoDBAttribute(attributeName = "ObaBaseUrl")
    public String getObaBaseUrl() { return obaBaseUrl; }
    public void setObaBaseUrl(String obaBaseUrl) { this.obaBaseUrl = obaBaseUrl; }
    
    @DynamoDBAttribute(attributeName = "CurrentStopId")
    public String getCurrentStopId() { return currentStopId; }
    public void setCurrentStopId(String currentStopId) { this.currentStopId = currentStopId; }
    
    @DynamoDBAttribute(attributeName = "CurrentStopCode")
    public String getCurrentStopCode() { return currentStopCode; }
    public void setCurrentStopCode(String currentStopCode) { this.currentStopCode = currentStopCode; }
    
    @DynamoDBAttribute(attributeName = "RouteFilter")
    public String getRouteFilter() { return routeFilter; }
    public void setrRouteFilter(String routeFilter) { this.routeFilter = routeFilter; } 
}