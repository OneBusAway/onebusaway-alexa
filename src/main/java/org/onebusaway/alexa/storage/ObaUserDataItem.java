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

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBVersionAttribute;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.HashSet;

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

    /**
     * Cached previous response spoken by Alexa to the user, triggered via the AAMAZON.RepeatIntent
     */
    @Getter
    @Setter
    @DynamoDBAttribute(attributeName = "PreviousResponse")
    private String previousResponse;

    @Getter
    @Setter
    @DynamoDBAttribute(attributeName = "LastAccessTime")
    private long lastAccessTime;

    /**
     * 0 for false, 1 for true (I thought DynamoDB didn't persist booleans at the time - FIXME?)
     */
    @Getter
    @Setter
    @DynamoDBAttribute(attributeName = "SpeakClockTime")
    private long speakClockTime;

    @Getter
    @Setter
    @DynamoDBAttribute(attributeName = "TimeZone")
    private String timeZone;

    /**
     * A set of stops (stop_id is the key) that are each mapped to a set of routeIds that should NOT
     * be read to the user.  routeIds can change when GTFS data changes, so we need to allow new routeIds to surface if
     * they haven't been seen before, and then the user can choose to filter them out.
     */
    @Getter
    @Setter
    @DynamoDBAttribute(attributeName = "RoutesToFilterOut")
    private HashMap<String, HashSet<String>> routesToFilterOut;

    /**
     * Whether or not we've given the user the introduction to OneBusAway when they first use the skill
     * <p>
     * 0 for false, 1 for true (I thought DynamoDB didn't persist booleans at the time - FIXME?)
     */
    @Getter
    @Setter
    @DynamoDBAttribute(attributeName = "AnnouncedIntroduction")
    private long announcedIntroduction;

    /**
     * Whether or not we've given the user an update for what's new in v1.1.0
     * <p>
     * 0 for false, 1 for true (I thought DynamoDB didn't persist booleans at the time - FIXME?)
     */
    @Getter
    @Setter
    @DynamoDBAttribute(attributeName = "AnnouncedFeatures-v1_1_0")
    private long announcedFeaturesv1_1_0;

    /**
     * Whether or not the user should hear information for experimental regions
     * <p>
     */
    @Getter
    @Setter
    @DynamoDBAttribute(attributeName = "ExperimentalRegions")
    private boolean experimentalRegions;

    @DynamoDBVersionAttribute
    private Long version;
}