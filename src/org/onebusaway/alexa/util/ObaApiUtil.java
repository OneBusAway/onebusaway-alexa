/*
 * Copyright (C) 2015 Sean J. Barbeau (sjbarbeau@gmail.com).
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
package org.onebusaway.alexa.util;

import java.io.IOException;
import java.net.URISyntaxException;

import org.onebusaway.io.client.elements.ObaArrivalInfo;
import org.onebusaway.io.client.elements.ObaStop;
import org.onebusaway.io.client.request.ObaArrivalInfoRequest;
import org.onebusaway.io.client.request.ObaArrivalInfoResponse;
import org.onebusaway.io.client.request.ObaStopsForLocationRequest;
import org.onebusaway.io.client.request.ObaStopsForLocationResponse;
import org.onebusaway.location.Location;

/**
 * Utilities for retrieving items from the OneBusAway REST APIs
 * 
 * @author barbeau
 */
public class ObaApiUtil {
	
	public static final int DEFAULT_SEARCH_RADIUS = 40000;

	/**
	 * Returns a list of nearby stops for the given location
	 * @param l location to search for nearby stops
	 * @return a list of nearby stops for the given location
	 * @throws IOException
	 */
	public static ObaStop[] getNearbyStops(Location l) throws IOException {
		ObaStopsForLocationResponse response = null;
		try {
			response = new ObaStopsForLocationRequest.Builder(l)
					.setRadius(DEFAULT_SEARCH_RADIUS)
			        .build()
			        .call();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
        return response.getStops();
	}

	/**
	 * Returns a stop for the given stopCode (user-facing stop ID), near the given location
	 * @param l Location to search near
	 * @param stopCode User-facing stop ID to search for
	 * @return a stop for the given stopCode (user-facing stop ID), near the given location
	 * @throws IOException
	 */
	public static ObaStop[] getStopFromCode(Location l, String stopCode) throws IOException {
		ObaStopsForLocationResponse response = null;
		try {
			response = new ObaStopsForLocationRequest.Builder(l)
					.setQuery(stopCode)
					.setRadius(DEFAULT_SEARCH_RADIUS)
			        .build()
			        .call();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
        return response.getStops();
	}
	
	/**
	 * Returns the arrivals and departures for the given stopId
	 * @param stopId the stopId to return arrivals and departures for
	 * @return the arrivals and departures for the given stopId
	 * @throws IOException
	 */
	public static ObaArrivalInfo[] getArrivalsAndDeparturesForStop(String stopId) throws IOException {
		ObaArrivalInfoResponse response = null;
		try {
			response = new ObaArrivalInfoRequest.Builder(stopId)
			        .build()
			        .call();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
        return response.getArrivalInfo();
	}
}
