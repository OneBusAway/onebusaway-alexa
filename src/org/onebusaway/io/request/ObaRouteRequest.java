/*
 * Copyright (C) 2010 Paul Watts (paulcwatts@gmail.com)
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
package org.onebusaway.io.request;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Callable;

/**
 * Retrieve info about a specific route
 * {@link http://code.google.com/p/onebusaway/wiki/OneBusAwayRestApi_Route}
 *
 * @author Paul Watts (paulcwatts@gmail.com)
 */
public final class ObaRouteRequest extends RequestBase implements Callable<ObaRouteResponse> {

    protected ObaRouteRequest(URI uri) {
        super(uri);
    }

    public static class Builder extends RequestBase.BuilderBase {

        public Builder(String routeId) throws UnsupportedEncodingException {
            super(getPathWithId("/route/", routeId));
        }

        public ObaRouteRequest build() throws URISyntaxException {
            return new ObaRouteRequest(buildUri());
        }
    }

    /**
     * Helper method for constructing new instances.
     *
     * @param context The package context.
     * @param routeId The route Id to request.
     * @return The new request instance.
     * @throws UnsupportedEncodingException 
     * @throws URISyntaxException 
     */
    public static ObaRouteRequest newRequest(String routeId) throws UnsupportedEncodingException, URISyntaxException {
        return new Builder(routeId).build();
    }

    @Override
    public ObaRouteResponse call() {
        return call(ObaRouteResponse.class);
    }

    @Override
    public String toString() {
        return "ObaRouteRequest [mUri=" + mUri + "]";
    }
}
