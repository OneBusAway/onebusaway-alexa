/*
 * Copyright 2016-2019 Sean J. Barbeau (sjbarbeau@gmail.com),
 * Philip M. White (philip@mailworks.org)
 * Chunzhang Mo (victormocz@gmail.com)
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onebusaway.alexa.personalization;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

/**
 * Model for skill personalization information.
 */
@AllArgsConstructor
@Data
@ToString
public class PersonalizationInfo {
    /**
     * principleId will be personId when personId is available in first request, otherwise it is the userId.
     */
    private String principleId;
    /**
     * user level Id.
     */
    private String userId;
    /**
     * personId in the request, will be empty("") if there is no personId.
     */
    private String personId;
    /**
     * Flag to tell if interaction personalized or not.
     */
    private boolean isPersonalized;
}
