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

package org.onebusaway.alexa.handlers.event;

import org.onebusaway.alexa.handlers.BaseHandler;

/**
 * Superclass for all Alexa events (e.g. skill disable event, enable event).
 */
abstract public class EventHandler extends BaseHandler {
    /**
     * {@inheritDoc}
     */
    @Override
    public String getRequestName() {
        return getEventRequestName();
    }

    /**
     * Returns the event request name.
     *
     * @return event request name
     */
    abstract public String getEventRequestName();
}
