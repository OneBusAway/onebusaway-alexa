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

package util;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.*;
import org.onebusaway.alexa.ObaIntent;

import java.lang.reflect.Field;
import java.util.HashMap;

import static org.junit.Assert.assertNotNull;
import static org.onebusaway.alexa.SessionAttribute.CITY_NAME;
import static org.onebusaway.alexa.SessionAttribute.STOP_NUMBER;

/**
 * Utilities to assist in testing the OBA Alexa skill
 */
public class TestUtil {

    /**
     * Check to make sure that the provided speechlet correctly handles all intents defined in ObaIntent
     *
     * @param speechlet
     * @param session
     * @throws SpeechletException
     */
    public static void assertAllIntents(Speechlet speechlet, Session session) throws SpeechletException, IllegalAccessException {
        Field[] fields = ObaIntent.class.getFields();
        SpeechletResponse sr;

        // Set up required slot values to avoid NPEs
        HashMap<String, Slot> slots = new HashMap<>();
        slots.put(CITY_NAME, Slot.builder()
                .withName(CITY_NAME)
                .withValue("Tampa").build());
        slots.put(STOP_NUMBER, Slot.builder()
                .withName(STOP_NUMBER)
                .withValue("6497").build());

        // Loop through all intents using reflection to make sure we're handling them
        for (Field f : fields) {
            sr = speechlet.onIntent(
                    IntentRequest.builder()
                            .withRequestId("test-request-id")
                            .withIntent(
                                    Intent.builder()
                                            .withName(f.get(String.class).toString())
                                            .withSlots(slots)
                                            .build()
                            )
                            .build(),
                    session
            );
            assertNotNull(sr);
        }
    }
}
