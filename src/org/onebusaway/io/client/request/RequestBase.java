/*
 * Copyright (C) 2010-2012 Paul Watts (paulcwatts@gmail.com)
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

import org.apache.commons.lang3.StringUtils;
import org.onebusaway.io.client.ObaApi;
import org.onebusaway.io.client.ObaConnection;
import org.onebusaway.io.client.ObaContext;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import javax.ws.rs.core.UriBuilder;

/**
 * The base class for Oba requests.
 *
 * @author Paul Watts (paulcwatts@gmail.com)
 */
public class RequestBase {

    private static final String TAG = "RequestBase";
    
    private static final String UTF8 = "UTF-8";

    protected final URI mUri;

    protected final String mPostData;

    protected RequestBase(URI uri) {
        mUri = uri;
        mPostData = null;
    }

    protected RequestBase(URI uri, String postData) {
        mUri = uri;
        mPostData = postData;
    }

    public static class BuilderBase {

        protected static final String BASE_PATH = "api/where";

        protected final UriBuilder mBuilder;

        protected ObaContext mObaContext;

        protected BuilderBase(String path) {
            this(null, path);
        }

        protected BuilderBase(ObaContext obaContext, String path) {
            mObaContext = obaContext;
            mBuilder = UriBuilder.fromPath(path);
        }

        protected static String getPathWithId(String pathElement, String id) throws UnsupportedEncodingException {
            StringBuilder builder = new StringBuilder(BASE_PATH);
            builder.append(pathElement);
            builder.append(URLEncoder.encode(id, UTF8));
            builder.append(".json");
            return builder.toString();
        }

        protected URI buildUri() throws URISyntaxException {
            ObaContext context = (mObaContext != null) ? mObaContext : ObaApi.getDefaultContext();
            context.setBaseUrl(mBuilder);
            context.setAppInfo(mBuilder);
            mBuilder.queryParam("version", "2");
            mBuilder.queryParam("key", context.getApiKey());
            return mBuilder.build();
        }

        public ObaContext getObaContext() {
            if (mObaContext == null) {
                mObaContext = ObaApi.getDefaultContext().clone();
            }
            return mObaContext;
        }
    }

    /**
     * Subclass for BuilderBase that can handle post data as well.
     *
     * @author paulw
     */
    public static class PostBuilderBase extends BuilderBase {

        protected final UriBuilder mPostData;

        protected PostBuilderBase(String path) {
            super(path);
            mPostData = UriBuilder.fromPath(path);
        }

        public String buildPostData() {
            return mPostData.build().getQuery();
        }
    }

    protected <T> T call(Class<T> cls) {
        ObaApi.SerializationHandler handler = ObaApi.getSerializer(cls);
        ObaConnection conn = null;
        try {
            conn = ObaApi.getDefaultContext().getConnectionFactory().newConnection(mUri);
            Reader reader;
            if (mPostData != null) {
                reader = conn.post(mPostData);
            } else {
                int responseCode = conn.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    return handler.createFromError(cls, responseCode, "");
                }
                reader = conn.get();
            }
            T t = handler.deserialize(reader, cls);
            if (t == null) {
                t = handler.createFromError(cls, ObaApi.OBA_INTERNAL_ERROR, "Json error");
            }
            return t;
        } catch (FileNotFoundException e) {
            System.err.println(e.toString());
            return handler.createFromError(cls, ObaApi.OBA_NOT_FOUND, e.toString());
        } catch (IOException e) {
        	System.err.println(e.toString());
            return handler.createFromError(cls, ObaApi.OBA_IO_EXCEPTION, e.toString());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    protected <T> T callPostHack(Class<T> cls) {
        ObaApi.SerializationHandler handler = ObaApi.getSerializer(cls);
        ObaConnection conn = null;
        try {
            conn = ObaApi.getDefaultContext().getConnectionFactory().newConnection(mUri);
            BufferedReader reader = new BufferedReader(conn.post(mPostData), 8 * 1024);

            String line;
            StringBuffer text = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                text.append(line + "\n");
            }

            String response = text.toString();
            if (StringUtils.isEmpty(response)) {
                return handler.createFromError(cls, ObaApi.OBA_OK, "OK");
            } else {
                // {"actionErrors":[],"fieldErrors":{"stopId":["requiredField.stopId"]}}
                // TODO: Deserialize the JSON and check "fieldErrors"
                // if this is empty, then it succeeded? Or check for an actual ObaResponse???
                return handler.createFromError(cls, ObaApi.OBA_INTERNAL_ERROR, response);
            }

        } catch (FileNotFoundException e) {
        	System.err.println(e.toString());
            return handler.createFromError(cls, ObaApi.OBA_NOT_FOUND, e.toString());
        } catch (IOException e) {
        	System.err.println(e.toString());
            return handler.createFromError(cls, ObaApi.OBA_IO_EXCEPTION, e.toString());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
