/*
 * Copyright 2016 Philip M. White (philip@mailworks.org)
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

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.*;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.onebusaway.alexa.lib.ObaAgencies;
import org.onebusaway.alexa.lib.ObaUserClient;
import org.onebusaway.alexa.storage.ObaUserDataItem;
import org.onebusaway.io.client.elements.ObaArrivalInfo;
import org.onebusaway.io.client.util.ArrivalInfo;

import javax.annotation.Resource;
import java.time.Instant;

@NoArgsConstructor
@Log4j
public class AuthedSpeechlet implements Speechlet {
    @Resource
    private AnonSpeechlet anonSpeechlet;

    @Resource
    private ObaAgencies obaAgencies;

    private ObaUserClient obaUserClient;

    private ObaUserDataItem userData;

    public void setUserData(ObaUserDataItem userData) {
        this.userData = userData;
        obaUserClient = new ObaUserClient(
                obaAgencies.agencyForCity(userData.getCity()).get());
    }

    @Override
    public SpeechletResponse onIntent(final IntentRequest request,
                                      final Session session)
                                      throws SpeechletException {
        Intent intent = request.getIntent();
        if ("SetCityIntent".equals(intent.getName())) {
            return anonSpeechlet.onIntent(request, session);
        } else if ("GetCityIntent".equals(intent.getName())) {
            PlainTextOutputSpeech out = new PlainTextOutputSpeech();
            out.setText(
                    String.format("You live in %s.",
                            userData.getCity()));
            return SpeechletResponse.newTellResponse(out);
        } else if ("SetStopNumberIntent".equals(intent.getName())) {
            return anonSpeechlet.onIntent(request, session);
        } else if ("GetArrivalsIntent".equals(intent.getName())) {
            return tellArrivals();
        } else {
            throw new SpeechletException("Did not recognize intent name");
        }
    }

    @Override
    public SpeechletResponse onLaunch(final LaunchRequest request,
                                      final Session session)
                                      throws SpeechletException {
        return tellArrivals();
    }

    @Override
    public void onSessionStarted(final SessionStartedRequest request,
                                 final Session session) {

    }

    @Override
    public void onSessionEnded(final SessionEndedRequest request,
                               final Session session) {

    }

    private SpeechletResponse tellArrivals() {
        ObaArrivalInfo[] arrivals = obaUserClient.getArrivalsAndDeparturesForStop(userData.getStopId());
        StringBuilder sb = new StringBuilder();
        for (ObaArrivalInfo obaArrival: arrivals) {
            log.info("Arrival: " + obaArrival);
            ArrivalInfo arrival = new ArrivalInfo(obaArrival, Instant.now().toEpochMilli());
            sb.append(arrival.getLongDescription() + " -- "); //with pause between sentences
        }
        log.info("Full text output: " + sb.toString());
        PlainTextOutputSpeech out = new PlainTextOutputSpeech();
        out.setText(sb.toString());
        return SpeechletResponse.newTellResponse(out);
    }
}
