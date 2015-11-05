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

import org.onebusaway.location.Location;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;

/**
 * Utilities for accessing Google web APIs
 * 
 * @author barbeau
 */
public class GoogleApiUtil {
	
	public static final String GEOCODE_API_KEY = "AIzaSyA6PiJXUmxic5fkIC4WEPIBldi6omdVZnQ";
	
	/**
	 * Returns the location of a city using the Google Geocoding API
	 * @param cityName city to geocode
	 * @return the location of a city using the Google Geocoding API
	 * @throws Exception
	 */
	public static Location geocode(String cityName) throws Exception {
		GeoApiContext context = new GeoApiContext().setApiKey(GEOCODE_API_KEY);
		GeocodingResult[] results =  GeocodingApi.geocode(context, cityName).await();
		Location l = new Location("Google Geocoding API");
		l.setLatitude(results[0].geometry.location.lat);
		l.setLongitude(results[0].geometry.location.lng);
		return l;
	}	
}
