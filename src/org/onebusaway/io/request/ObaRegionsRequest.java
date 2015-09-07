/*
 * Copyright (C) 2013 Paul Watts (paulcwatts@gmail.com)
 * and individual contributors
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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Callable;

public final class ObaRegionsRequest extends RequestBase implements
        Callable<ObaRegionsResponse> {

    protected ObaRegionsRequest(URI uri) {
        super(uri);
    }

    //
    // This currently has a very simple builder because you can't do much with this "API"
    //
    public static class Builder {

        private static URI URI;

        public Builder() throws URISyntaxException {
        	URI = new URI("http://regions.onebusaway.org/regions-v3.json");
        }

        public Builder(URI uri) {
            URI = uri;
        }

        public ObaRegionsRequest build() {
            return new ObaRegionsRequest(URI);
        }
    }

    /**
     * Helper method for constructing new instances.
     *
     * @return The new request instance.
     * @throws URISyntaxException 
     */
    public static ObaRegionsRequest newRequest() throws URISyntaxException {
        return new Builder().build();
    }

    /**
     * Helper method for constructing new instances, allowing
     * the requester to set the URI to retrieve the regions info
     * from
     *
     * @param context The package context.
     * @param uri     URI to the regions file
     * @return The new request instance.
     */
    public static ObaRegionsRequest newRequest(URI uri) {
        return new Builder(uri).build();
    }

    @Override
    public ObaRegionsResponse call() {
        return call(ObaRegionsResponse.class);
    }

    @Override
    public String toString() {
        return "ObaRegionsRequest [mUri=" + mUri + "]";
    }
}
