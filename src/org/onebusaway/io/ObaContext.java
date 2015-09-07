/*
 * Copyright (C) 2012 Paul Watts (paulcwatts@gmail.com)
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
package org.onebusaway.io;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.ws.rs.core.UriBuilder;

import org.onebusaway.io.elements.ObaRegion;

public class ObaContext {

    private static final String TAG = "ObaContext";

    private String mApiKey = "TEST";

    private int mAppVer = 0;

    private String mAppUid = null;

    private ObaConnectionFactory mConnectionFactory = ObaDefaultConnectionFactory.getInstance();

    private ObaRegion mRegion;

    public ObaContext() {
    }

    public void setAppInfo(int version, String uuid) {
        mAppVer = version;
        mAppUid = uuid;
    }

    public void setAppInfo(UriBuilder builder) {
        if (mAppVer != 0) {
            builder.queryParam("app_ver", String.valueOf(mAppVer));
        }
        if (mAppUid != null) {
            builder.queryParam("app_uid", mAppUid);
        }
    }

    public void setApiKey(String apiKey) {
        mApiKey = apiKey;
    }

    public String getApiKey() {
        return mApiKey;
    }

    public void setRegion(ObaRegion region) {
        mRegion = region;
    }

    public ObaRegion getRegion() {
        return mRegion;
    }

    /**
     * Connection factory
     */
    public ObaConnectionFactory setConnectionFactory(ObaConnectionFactory factory) {
        ObaConnectionFactory prev = mConnectionFactory;
        mConnectionFactory = factory;
        return prev;
    }

    public ObaConnectionFactory getConnectionFactory() {
        return mConnectionFactory;
    }

    public void setBaseUrl(UriBuilder builder) throws URISyntaxException {
//        URI baseUrl = null;
//        System.out.println("Using region base URL '" + mRegion.getObaBaseUrl() + "'.");
//
//        baseUrl = Uri.parse(mRegion.getObaBaseUrl());
//    
//        // Copy partial path (if one exists) from the base URL
//        UriBuilder path = new UriBuilder();
//        path.encodedPath(baseUrl.getEncodedPath());
//
//        // Then, tack on the rest of the REST API method path from the Uri.Builder that was passed in
//        path.appendEncodedPath(builder.build().getPath());
//
//        // Finally, overwrite builder that was passed in with the full URL
//        builder.scheme(baseUrl.getScheme());
//        builder.encodedAuthority(baseUrl.getEncodedAuthority());
//        builder.encodedPath(path.build().getEncodedPath());
	      URI baseUrl = null;
	      System.out.println("Using region base URL '" + mRegion.getObaBaseUrl() + "'.");
	
	      baseUrl = new URI(mRegion.getObaBaseUrl());
	  
	      // Copy partial path (if one exists) from the base URL
	      UriBuilder path = UriBuilder.fromPath(baseUrl.getPath());
	
	      // Then, tack on the rest of the REST API method path from the Uri.Builder that was passed in
	      path.path(builder.build().getPath());
	
	      // Finally, overwrite builder that was passed in with the full URL
	      builder.uri(baseUrl);
	      builder.replacePath(path.build().getPath());
    }

    @Override
    public ObaContext clone() {
        ObaContext result = new ObaContext();
        result.setApiKey(mApiKey);
        result.setAppInfo(mAppVer, mAppUid);
        result.setConnectionFactory(mConnectionFactory);
        return result;
    }
}
