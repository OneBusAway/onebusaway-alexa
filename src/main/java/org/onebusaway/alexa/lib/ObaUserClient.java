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
import org.onebusaway.io.client.elements.ObaStop;
import org.onebusaway.io.client.request.ObaArrivalInfoRequest;
import org.onebusaway.io.client.request.ObaArrivalInfoResponse;
import org.onebusaway.io.client.request.ObaStopsForLocationRequest;
import org.onebusaway.io.client.request.ObaStopsForLocationResponse;
import org.onebusaway.location.Location;

import java.io.IOException;
import java.net.URISyntaxException;

@Log4j
/* FIXME: This class gives the illusion of object encapsulation,
 * but it actually manipulates the global context of ObaApi.
 * I don't yet know the ObaApi well enough to fix this.
 */
public class ObaUserClient {
    private static final int DEFAULT_SEARCH_RADIUS_METERS = 40000;

    public ObaUserClient(String endpoint) {
        log.debug("Instantiating ObaUserClient with endpoint " + endpoint);
        try {
            ObaApi.getDefaultContext().setBaseUrl(
                    String.format("http://api.%s.onebusaway.org/", endpoint));
        } catch (URISyntaxException e) {
            log.error("OBA Endpoint we constructed was invalid: " + e.getMessage());
        } catch (Exception e) {
            log.error("Exception setting OBA base URL: " + e.getMessage());
            throw e;
        }
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
     * @return the arrival info response for the given stopId
     */
    public ObaArrivalInfoResponse getArrivalsAndDeparturesForStop(String stopId) {
        return new ObaArrivalInfoRequest.Builder(stopId)
                .build()
                .call();
    }
}
