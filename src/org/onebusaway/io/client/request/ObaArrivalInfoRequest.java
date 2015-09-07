/*
 * Copyright (C) 2010-2013 Paul Watts (paulcwatts@gmail.com)
 * and individual contributors.
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
package org.onebusaway.io.client.request;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Callable;

public final class ObaArrivalInfoRequest extends RequestBase implements
        Callable<ObaArrivalInfoResponse> {

    protected ObaArrivalInfoRequest(URI uri) {
        super(uri);
    }

    public static class Builder extends RequestBase.BuilderBase {

        public Builder(String stopId) throws UnsupportedEncodingException {
            super(getPathWithId("/arrivals-and-departures-for-stop/", stopId));
        }

        public Builder(String stopId, int minutesAfter) throws UnsupportedEncodingException {
            super(getPathWithId("/arrivals-and-departures-for-stop/", stopId));
            mBuilder.queryParam("minutesAfter", String.valueOf(minutesAfter));
        }

        public ObaArrivalInfoRequest build() throws URISyntaxException {
            return new ObaArrivalInfoRequest(buildUri());
        }
    }

    /**
     * Helper method for constructing new instances.
     *
     * @param context The package context.
     * @param stopId  The stop Id to request.
     * @return The new request instance.
     * @throws UnsupportedEncodingException 
     * @throws URISyntaxException 
     */
    public static ObaArrivalInfoRequest newRequest(String stopId) throws UnsupportedEncodingException, URISyntaxException {
        return new Builder(stopId).build();
    }

    /**
     * Helper method for constructing new instances.
     *
     * @param context      The package context.
     * @param stopId       The stop Id to request.
     * @param minutesAfter includes vehicles arriving or departing in the next minutesAfter minutes
     * @return The new request instance.
     * @throws UnsupportedEncodingException 
     * @throws URISyntaxException 
     */
    public static ObaArrivalInfoRequest newRequest(String stopId,
            int minutesAfter) throws UnsupportedEncodingException, URISyntaxException {
        return new Builder(stopId, minutesAfter).build();
    }

    @Override
    public ObaArrivalInfoResponse call() {
        return call(ObaArrivalInfoResponse.class);
    }

}
