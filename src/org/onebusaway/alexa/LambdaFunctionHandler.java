
package org.onebusaway.alexa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;

import javax.ws.rs.core.UriBuilder;

import org.apache.commons.io.IOUtils;
import org.onebusaway.io.client.ObaApi;
import org.onebusaway.io.client.elements.ObaRegion;
import org.onebusaway.io.client.elements.ObaStop;
import org.onebusaway.io.client.request.ObaRegionsRequest;
import org.onebusaway.io.client.request.ObaRegionsResponse;
import org.onebusaway.io.client.request.ObaStopsForLocationRequest;
import org.onebusaway.io.client.request.ObaStopsForLocationResponse;
import org.onebusaway.io.client.util.RegionUtils;
import org.onebusaway.location.Location;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletRequest;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;

/**
 * 
 * @author @author barbeau
 *
 */
public class LambdaFunctionHandler implements RequestStreamHandler {
	
	@Override
	public void handleRequest(InputStream inputStream, OutputStream output, Context context) throws IOException {
    	byte serializedSpeechletRequest[] = IOUtils.toByteArray(inputStream);
    	SpeechletRequestEnvelope requestEnvelope = SpeechletRequestEnvelope.fromJson(serializedSpeechletRequest);

        SpeechletRequest speechletRequest = requestEnvelope.getRequest();
//        Session session = requestEnvelope.getSession();
//        String requestId = speechletRequest == null ? null : speechletRequest.getRequestId();
  
        if (speechletRequest instanceof IntentRequest) {
        	IntentRequest ir = (IntentRequest) speechletRequest;
        	String outString = "IntentRequest name: " + ir.getIntent().getName() + "\n";
        	context.getLogger().log(outString);
        	output.write(outString.getBytes());
        }
        
        String zipCode = "33613";
        Location location = null;
        try {
			location = GoogleApiUtil.geocode(zipCode);
			
			String latLng = "Lat/long for zip " + zipCode + " = " + location.getLatitude() + ", " + location.getLongitude() + "\n";
			output.write(latLng.getBytes());
		} catch (Exception e1) {
			e1.printStackTrace();
		}
        
        ObaApi.getDefaultContext().setApiKey("TEST");
        ObaRegionsResponse response = null;
		try {
			response = ObaRegionsRequest.newRequest().call();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
        ArrayList<ObaRegion> regions = new ArrayList<ObaRegion>(Arrays.asList(response.getRegions()));
        
        if (location != null) {
        	ObaRegion r = RegionUtils.getClosestRegion(regions, location);
        	if (r != null) {
        		ObaApi.getDefaultContext().setRegion(r);
        		
        		ObaStop[] nearbyStops = ObaApiUtil.getNearbyStops(location);
        		
        		output.write("Nearby stops:\n".getBytes());
                for (ObaStop s : nearbyStops) {
                	String outString = s.getName() + "\n";
                    output.write(outString.getBytes());
                }
                
                String stopCode = "3105";
                ObaStop[] searchResults = ObaApiUtil.getStopFromCode(location, stopCode);
                output.write("Search result:\n".getBytes());
                for (ObaStop s : searchResults) {
                	String outString = s.getName() + "\n";
                    output.write(outString.getBytes());
                }
        	}
        }
	}
}
