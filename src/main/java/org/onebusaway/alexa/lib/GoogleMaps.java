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

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.log4j.Log4j;
import org.onebusaway.location.Location;

import java.util.Optional;

/**
 * Utilities for accessing Google web APIs
 */
@AllArgsConstructor
@Log4j
public class GoogleMaps {
	@Getter
	private String apiKey;

	/**
	 * Returns the location of a city using the Google Geocoding API
	 * 
	 * @param cityName city to geocode
	 * @return the location of a city using the Google Geocoding API, or null if the location
	 *         couldn't be geocoded.
	 */
	public Optional<Location> geocode(String cityName) {
		log.debug("Entered Google API");
		GeoApiContext context = new GeoApiContext().setApiKey(getApiKey());
		GeocodingResult[] results = null;
		try {
			results = GeocodingApi.geocode(context, cityName).await();
		} catch (Exception e) {
			log.error("Got exception from GeocodingApi: " + e);
			e.printStackTrace();
			return Optional.empty();
		}
		Location l = new Location("Google Geocoding API");
		l.setLatitude(results[0].geometry.location.lat);
		l.setLongitude(results[0].geometry.location.lng);
		log.debug(String.format("Google API returned %s.", l));
		return Optional.of(l);
	}
}
