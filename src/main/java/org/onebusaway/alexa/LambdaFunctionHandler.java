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
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.onebusaway.alexa.util.GoogleApiUtil;
import org.onebusaway.alexa.util.ObaApiUtil;
import org.onebusaway.io.client.ObaApi;
import org.onebusaway.io.client.elements.ObaArrivalInfo;
import org.onebusaway.io.client.elements.ObaRegion;
import org.onebusaway.io.client.elements.ObaStop;
import org.onebusaway.io.client.request.ObaArrivalInfoResponse;
import org.onebusaway.io.client.request.ObaRegionsRequest;
import org.onebusaway.io.client.request.ObaRegionsResponse;
import org.onebusaway.io.client.util.ArrivalInfo;
import org.onebusaway.io.client.util.RegionUtils;
import org.onebusaway.location.Location;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

/**
 * OneBusAway Alexa - main handler to receive and process messages from Alexa
 * via Lambda
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
		// String requestId = speechletRequest == null ? null :
		// speechletRequest.getRequestId();
		String userId = "User ID = " + session.getUser().getUserId() + "\n";
		output.write(userId.getBytes());

		// if (speechletRequest instanceof IntentRequest) {
		// IntentRequest ir = (IntentRequest) speechletRequest;
		// String outString = "IntentRequest name: " + ir.getIntent().getName()
		// + "\n";
		// context.getLogger().log(outString);
		// output.write(outString.getBytes());
		// }

		/**
		 * OneBusAway: "OneBusAway.  What city are you located in?"
		 * 
		 * User: "Seattle"
		 */
		String cityName = "Seattle";
		Location location = GoogleApiUtil.geocode(cityName);
		String latLng = "Lat/long for " + cityName + " = " + location.getLatitude() + ", " + location.getLongitude()
				+ "\n";
		output.write(latLng.getBytes());

		ObaApi.getDefaultContext().setApiKey("TEST");
		ObaRegionsResponse response = null;
		response = ObaRegionsRequest.newRequest().call();
		ArrayList<ObaRegion> regions = new ArrayList<ObaRegion>(Arrays.asList(response.getRegions()));

		if (location != null) {
			ObaRegion r = RegionUtils.getClosestRegion(regions, location);
			if (r != null) {
				ObaApi.getDefaultContext().setRegion(r);

				/**
				 * OneBusAway:
				 * "You can find your bus stop number on the sign near your stop. What is your stop number?"
				 *
				 * User: 3105
				 */

				// Search for a stop with a specific ID
				String stopCode = "431";
				ObaStop[] searchResults = ObaApiUtil.getStopFromCode(location, stopCode);
				output.write("Stop search result:\n".getBytes());
				for (ObaStop s : searchResults) {
					String outString = s.getName() + "\n";
					output.write(outString.getBytes());

					// Get arrival info for stop - convert from raw data to more
					// human readable form
					ObaArrivalInfoResponse arrivalsResponse = ObaApiUtil.getArrivalsAndDeparturesForStop(s.getId());
					ObaArrivalInfo[] info = arrivalsResponse.getArrivalInfo();
					long currentTime = arrivalsResponse.getCurrentTime();
					ArrayList<ArrivalInfo> infoList = ArrivalInfo.convertObaArrivalInfo(info, new ArrayList<String>(),
							currentTime);
					// Print out route and ETA for all arrivals
					for (ArrivalInfo i : infoList) {
						outString = i.getLongDescription() + "\n";
						output.write(outString.getBytes());
					}
				}
			}
		}
	}
}
