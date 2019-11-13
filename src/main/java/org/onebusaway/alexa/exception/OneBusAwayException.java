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

package org.onebusaway.alexa.exception;

/**
 * OneBusAway exception will be thrown when Alexa handler fails to process the request.
 */
public class OneBusAwayException extends RuntimeException {
    public OneBusAwayException() {
    }

    public OneBusAwayException(String message) {
        super(message);
    }

    public OneBusAwayException(String message, Throwable cause) {
        super(message, cause);
    }
}
