/*
 * Copyright (C) 2015 Sean J. Barbeau (sjbarbeau@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.alexa.lib;

import lombok.extern.log4j.Log4j;
import org.onebusaway.io.client.ObaApi;
import org.onebusaway.io.client.elements.ObaRegion;
import org.onebusaway.io.client.elements.ObaStop;
import org.onebusaway.io.client.request.*;
import org.onebusaway.io.client.util.RegionUtils;
import org.onebusaway.location.Location;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

@Log4j
/* FIXME: This class gives the illusion of object encapsulation,
 * but it actually manipulates the global context of ObaApi.
 * I don't yet know the ObaApi well enough to fix this.
 */
public class ObaUserClient {
    private static final int DEFAULT_SEARCH_RADIUS_METERS = 40000;

    public ObaUserClient(String obaBaseUrl) throws URISyntaxException {
        log.debug("Instantiating ObaUserClient with obaBaseUrl " + obaBaseUrl);
        try {
            ObaApi.getDefaultContext().setBaseUrl(obaBaseUrl);
        } catch (URISyntaxException e) {
            log.error("ObaBaseUrl we constructed was invalid: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Exception setting ObaBaseUrl: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Get the closest region to a given location.  Regions come from the Regions API
     * (http://regions.onebusaway.org/regions-v3.json), which is a centralized directory of all known
     * OneBusAway servers.  Each region has it's own OBA REST API endpoint
     * (http://developer.onebusaway.org/modules/onebusaway-application-modules/current/api/where/index.html),
     * which we will then use to get stop and arrival data for that region.
     *
     * @param l geographic location used to search for nearby regions
     * @return the closest region in the Regions API to the given location, or null if there are no nearby regions
     * (within 100 miles of the provided location) or a region couldn't be found.
     */
    public static Optional<ObaRegion> getClosestRegion(Location l) {
        log.debug("Invoked getClosestRegion() with location " + l.toString());
        ObaRegionsResponse response = ObaRegionsRequest.newRequest().call();
        ArrayList<ObaRegion> regions = new ArrayList<>(Arrays.asList(response.getRegions()));
        return Optional.ofNullable(RegionUtils.getClosestRegion(
                regions,
                l,
                true)); // enforce proximity threshold
    }

    /**
     * Returns a list of nearby stops for the given location
     *
     * @param l location to search for nearby stops
     * @return a list of nearby stops for the given location
     * @throws IOException
     */
    public ObaStop[] getNearbyStops(Location l) throws IOException {
        log.debug("Invoked getNearbyStops() with location " + l.toString());
        ObaStopsForLocationResponse response = new ObaStopsForLocationRequest.Builder(l)
                .setRadius(DEFAULT_SEARCH_RADIUS_METERS)
                .build()
                .call();
        log.debug("ObaStopsForLocationRequest returned " + response.toString());
        log.debug("  " + response.getStops().toString());
        return response.getStops();
    }

    /**
     * Returns a stop for the given stopCode (user-facing stop ID), near the
     * given location
     *
     * @param l        Location to search near
     * @param stopCode User-facing stop ID to search for
     * @return response that contains a stop for the given stopCode (user-facing
     * stop ID), near the given location
     */
    public ObaStop[] getStopFromCode(Location l, int stopCode) {
        log.debug("Invoked getStopFromCode() with location " + l.toString() + " and stopCode " + stopCode);
        ObaStopsForLocationResponse response = new ObaStopsForLocationRequest.Builder(l)
                .setQuery(String.format("%d", stopCode))
                .setRadius(DEFAULT_SEARCH_RADIUS_METERS)
                .build()
                .call();
        log.debug("response = " + response);
        return response.getStops();
    }

    /**
     * Returns the arrivals and departures for the given stopId
     *
     * @param stopId the stopId to return arrivals and departures for
     * @param scanMins number of minutes to look ahead for arrivals
     * @return the arrival info response for the given stopId
     */
    public ObaArrivalInfoResponse getArrivalsAndDeparturesForStop(String stopId, int scanMins) {
        return new ObaArrivalInfoRequest.Builder(stopId, scanMins)
                .build()
                .call();
    }
}
