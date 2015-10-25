package org.onebusaway.alexa;

import org.onebusaway.location.Location;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;

/**
 * Utilities for accessing Google web APIs
 * 
 * @author barbeau
 */
public class GoogleApiUtil {
	
	public static final String GEOCODE_API_KEY = "AIzaSyA6PiJXUmxic5fkIC4WEPIBldi6omdVZnQ";
	
	/**
	 * Returns the location of a zip code using the Google Geocoding API
	 * @param zipCode zip code to geocode
	 * @return the location of a zip code using the Google Geocoding API
	 * @throws Exception
	 */
	public static Location geocode(String zipCode) throws Exception {
		GeoApiContext context = new GeoApiContext().setApiKey(GEOCODE_API_KEY);
		GeocodingResult[] results =  GeocodingApi.geocode(context, zipCode).await();
		Location l = new Location("Google Geocoding API");
		l.setLatitude(results[0].geometry.location.lat);
		l.setLongitude(results[0].geometry.location.lng);
		return l;
	}	
}
