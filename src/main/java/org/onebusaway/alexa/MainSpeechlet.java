/*
 * Copyright 2016 Philip M. White (philip@mailworks.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.alexa;

import com.amazon.speech.speechlet.*;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.onebusaway.alexa.storage.ObaDao;
import org.onebusaway.alexa.storage.ObaUserDataItem;
import org.springframework.context.ApplicationContext;

import javax.annotation.Resource;
import java.util.Optional;

@NoArgsConstructor
@Log4j
public class MainSpeechlet implements Speechlet {
    @Resource
    private ApplicationContext appCxt;

    @Resource
    private ObaDao obaDao;

    @Resource
    private AnonSpeechlet anonSpeechlet;

    public SpeechletResponse onIntent(final IntentRequest request, final Session session) throws SpeechletException {
        try {
            Optional<ObaUserDataItem> optUserData = obaDao.getUserData(session);
            if (optUserData.isPresent()) {
                return getAuthedSpeechlet(optUserData.get()).onIntent(request, session);
            } else {
                return anonSpeechlet.onIntent(request, session);
            }
        } catch (Exception e) {
            log.error("Intent exception: " + e.getMessage());
            log.error("Backtrace:\n" + e.getStackTrace());
            throw e;
        }
    }

    public SpeechletResponse onLaunch(LaunchRequest request, Session session) throws SpeechletException {
        Optional<ObaUserDataItem> optUserData = obaDao.getUserData(session);
        if (optUserData.isPresent()) {
            return getAuthedSpeechlet(optUserData.get()).onLaunch(request, session);
        } else {
            return anonSpeechlet.onLaunch(request, session);
        }
    }

    public void onSessionStarted(SessionStartedRequest request, Session session) {
    }

    public void onSessionEnded(SessionEndedRequest request, Session session) {
    }

    private AuthedSpeechlet getAuthedSpeechlet(ObaUserDataItem obaUserDataItem) {
        // This is kinda a hack.  I'd like AuthedSpeechlet to require
        // `obaUserDataItem` as a constructor argument, but then I cannot
        // have Spring manage it.  Or can I?
        AuthedSpeechlet authedSpeechlet = (AuthedSpeechlet)appCxt.getBean("authedSpeechlet");
        authedSpeechlet.setUserData(obaUserDataItem);
        return authedSpeechlet;
    }
}