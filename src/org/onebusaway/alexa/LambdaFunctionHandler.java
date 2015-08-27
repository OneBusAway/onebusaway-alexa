package org.onebusaway.alexa;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

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
	}
}
