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
package org.onebusaway.alexa.lib;

import lombok.NonNull;
import lombok.extern.log4j.Log4j;
import org.onebusaway.io.client.ObaApi;
import org.onebusaway.io.client.elements.ObaAgencyWithCoverage;
import org.onebusaway.io.client.elements.ObaStop;
import org.onebusaway.io.client.request.*;
import org.onebusaway.location.Location;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.TimeZone;

@Log4j
/*
 * Client code used to access the OBA REST API for a local OBA server.  All methods in this class result in a REST API
 * call (i.e., network access).
 *
 * FIXME: This class gives the illusion of object encapsulation, but it actually manipulates the global context of
 * ObaApi. I don't yet know the ObaApi well enough to fix this.
 */
public class ObaUserClient extends ObaClientSharedCode {
    private static final int DEFAULT_SEARCH_RADIUS_METERS = 40000;
    public static final int ARRIVALS_SCAN_MINS = 65;

    public ObaUserClient(@NonNull String obaBaseUrl) throws URISyntaxException {
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
     * Returns a list of nearby stops for the given location
     *
     * @param l location to search for nearby stops
     * @return a list of nearby stops for the given location
     * @throws IOException
     */
    public ObaStop[] getNearbyStops(@NonNull Location l) throws IOException {
        log.debug("Invoked getNearbyStops() with location " + l.toString());
        ObaStopsForLocationResponse response = new ObaStopsForLocationRequest.Builder(l)
                .setRadius(DEFAULT_SEARCH_RADIUS_METERS)
                .build()
                .call();
        log.debug("ObaStopsForLocationRequest returned " + response.toString());
        log.debug("  " + response.getStops().toString());
        if (response.getCode() == ObaApi.OBA_OK) {
            return response.getStops();
        } else {
            throw new IOException(String.format("OBA Error %s getting stops for %s", response.getCode(), l.toString()));
        }
    }

    /**
     * Returns details about a particular stop, given it's stopId
     * @param stopId
     * @return details about a particular stop, given it's stopId
     */
    public ObaStopResponse getStopDetails(@NonNull String stopId) throws IOException {
        ObaStopResponse response = new ObaStopRequest.Builder(stopId).build().call();
        log.debug("ObaStopRequest returned " + response.toString());
        if (response.getCode() == ObaApi.OBA_OK) {
            return response;
        } else {
            throw new IOException(String.format("OBA Error %s getting stop details for %s", response.getCode(), stopId));
        }
    }

    /**
     * Returns a stop for the given stopCode (user-facing stop ID), near the
     * given location
     *
     * @param l        Location to search near
     * @param stopCode User-facing stop ID (i.e., GTFS stop_code) to search for
     * @return response that contains a stop for the given stopCode (user-facing
     * stop ID), near the given location
     */
    public ObaStop[] getStopFromCode(@NonNull Location l,
                                     String stopCode) throws IOException {
        log.debug("Invoked getStopFromCode() with location " + l.toString() + " and stopCode " + stopCode);
        ObaStopsForLocationResponse response = new ObaStopsForLocationRequest.Builder(l)
                .setQuery(stopCode)
                .setRadius(DEFAULT_SEARCH_RADIUS_METERS)
                .build()
                .call();
        log.debug("ObaStopsForLocationRequest returned = " + response);
        if (response.getCode() == ObaApi.OBA_OK) {
            return response.getStops();
        } else {
            throw new IOException(String.format("OBA Error %s getting stop details for %s at %s", response.getCode(), stopCode, l.toString()));
        }
    }

    /**
     * Returns the arrivals and departures for the given stopId
     *
     * @param stopId the stopId to return arrivals and departures for
     * @param scanMins number of minutes to look ahead for arrivals
     * @return the arrival info response for the given stopId
     */
    public ObaArrivalInfoResponse getArrivalsAndDeparturesForStop(@NonNull String stopId,
                                                                  int scanMins) throws IOException {
        ObaArrivalInfoResponse response = new ObaArrivalInfoRequest.Builder(stopId, scanMins)
                .build()
                .call();
        if (response.getCode() == ObaApi.OBA_OK) {
            return response;
        } else {
            throw new IOException(String.format("OBA Error %s getting arrivals and departures for %s", response.getCode(), stopId));
        }
    }

    /**
     * Returns time zone for the current OneBusAway region
     *
     * @return time zone for the current OneBusAway region
     */
    public TimeZone getTimeZone() throws IOException {
        ObaAgenciesWithCoverageResponse response = new ObaAgenciesWithCoverageRequest.Builder().build().call();
        log.debug("ObaAgenciesWithCoverageRequest returned " + response.toString());
        if (response.getCode() == ObaApi.OBA_OK) {
            ObaAgencyWithCoverage[] agencies = response.getAgencies();
            String timeZoneText = response.getRefs().getAgency(agencies[0].getId()).getTimezone();
            return TimeZone.getTimeZone(timeZoneText);
        } else {
            throw new IOException(String.format("OBA Error %s getting timezone", response.getCode()));
        }
    }
}
