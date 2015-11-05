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
package org.onebusaway.alexa;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.onebusaway.alexa.util.GoogleApiUtil;
import org.onebusaway.alexa.util.ObaApiUtil;
import org.onebusaway.io.client.ObaApi;
import org.onebusaway.io.client.elements.ObaRegion;
import org.onebusaway.io.client.elements.ObaStop;
import org.onebusaway.io.client.request.ObaRegionsRequest;
import org.onebusaway.io.client.request.ObaRegionsResponse;
import org.onebusaway.io.client.util.RegionUtils;
import org.onebusaway.location.Location;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

/**
 * OneBusAway Alexa - main handler to receive and process messages from Alexa via Lambda
 * 
 * @author barbeau
 *
 */
public class LambdaFunctionHandler implements RequestStreamHandler {
	
	@Override
	public void handleRequest(InputStream inputStream, OutputStream output, Context context) throws IOException {
    	byte serializedSpeechletRequest[] = IOUtils.toByteArray(inputStream);
    	SpeechletRequestEnvelope requestEnvelope = SpeechletRequestEnvelope.fromJson(serializedSpeechletRequest);

        SpeechletRequest speechletRequest = requestEnvelope.getRequest();
        Session session = requestEnvelope.getSession();
//        String requestId = speechletRequest == null ? null : speechletRequest.getRequestId();
        String userId = "User ID = " + session.getUser().getUserId() + "\n";
        output.write(userId.getBytes());
  
//        if (speechletRequest instanceof IntentRequest) {
//        	IntentRequest ir = (IntentRequest) speechletRequest;
//        	String outString = "IntentRequest name: " + ir.getIntent().getName() + "\n";
//        	context.getLogger().log(outString);
//        	output.write(outString.getBytes());
//        }
        
        String cityName = "Tampa";
        Location location = null;
        try {
			location = GoogleApiUtil.geocode(cityName);
			
			String latLng = "Lat/long for " + cityName + " = " + location.getLatitude() + ", " + location.getLongitude() + "\n";
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
