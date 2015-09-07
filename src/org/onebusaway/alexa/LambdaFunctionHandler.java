
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

import org.apache.commons.io.IOUtils;
import org.onebusaway.io.client.ObaApi;
import org.onebusaway.io.client.elements.ObaRegion;
import org.onebusaway.io.client.elements.ObaStop;
import org.onebusaway.io.client.request.ObaRegionsRequest;
import org.onebusaway.io.client.request.ObaRegionsResponse;
import org.onebusaway.io.client.request.ObaStopsForLocationRequest;
import org.onebusaway.io.client.request.ObaStopsForLocationResponse;
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
        	String outString = "IntentRequest name: " + ir.getIntent().getName();
        	context.getLogger().log(outString);
        	output.write(outString.getBytes());
        }
        
//        URL url = new URL("http://regions.onebusaway.org/regions-v3.json");
//        URLConnection yc = url.openConnection();
//        BufferedReader in = new BufferedReader(new InputStreamReader(
//                                    yc.getInputStream()));
//        String inputLine;
//        while ((inputLine = in.readLine()) != null) {
//        	inputLine = inputLine + "\n";
//            output.write(inputLine.getBytes());
//        }
//        
//        in.close();
        ObaRegionsResponse response = null;
		try {
			response = ObaRegionsRequest.newRequest().call();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
        ArrayList<ObaRegion> regions = new ArrayList<ObaRegion>(Arrays.asList(response.getRegions()));
        for (ObaRegion r : regions) {
        	if (r.getName().equalsIgnoreCase("Tampa")) {
        		ObaApi.getDefaultContext().setRegion(r);
        		Location l = new Location("Test");
        		l.setLatitude(28.0664191);
        		l.setLongitude(-82.4298721);
        		ObaStopsForLocationResponse response2 = null;
				try {
					response2 = new ObaStopsForLocationRequest.Builder(l)
					        .build()
					        .call();
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
                final ObaStop[] list = response2.getStops();
                for (ObaStop s : list) {
                	String outString = s.getName() + "\n";
                    output.write(outString.getBytes());
                }
        	}
        }
	}
}
