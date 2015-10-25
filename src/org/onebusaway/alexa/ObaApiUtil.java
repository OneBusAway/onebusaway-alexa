package org.onebusaway.alexa;

import java.io.IOException;
import java.net.URISyntaxException;

import org.onebusaway.io.client.elements.ObaStop;
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
		ObaStopsForLocationResponse response2 = null;
		try {
			response2 = new ObaStopsForLocationRequest.Builder(l)
					.setRadius(DEFAULT_SEARCH_RADIUS)
			        .build()
			        .call();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
        return response2.getStops();
	}

	/**
	 * Returns a stop for the given stopCode (user-facing stop ID), near the given location
	 * @param l Location to search near
	 * @param stopCode User-facing stop ID to search for
	 * @return a stop for the given stopCode (user-facing stop ID), near the given location
	 * @throws IOException
	 */
	public static ObaStop[] getStopFromCode(Location l, String stopCode) throws IOException {
		ObaStopsForLocationResponse response2 = null;
		try {
			response2 = new ObaStopsForLocationRequest.Builder(l)
					.setQuery(stopCode)
					.setRadius(DEFAULT_SEARCH_RADIUS)
			        .build()
			        .call();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
        return response2.getStops();
	}
}
