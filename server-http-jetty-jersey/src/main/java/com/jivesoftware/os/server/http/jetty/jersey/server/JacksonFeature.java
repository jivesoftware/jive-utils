/*
 * Copyright 2013 Jive Software, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.jivesoftware.os.server.http.jetty.jersey.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import org.glassfish.jersey.CommonProperties;

import static com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider.DEFAULT_ANNOTATIONS;

/** Feature used to register Jackson JSON providers. */
public class JacksonFeature implements Feature {

    private ObjectMapper mapper;

    public JacksonFeature withMapper(ObjectMapper mapper) {
        this.mapper = mapper;
        return this;
    }

    @Override
    public boolean configure(final FeatureContext context) {
        final String disableMoxy = CommonProperties.MOXY_JSON_FEATURE_DISABLE + '.' + context.getConfiguration().getRuntimeType().name().toLowerCase();
        context.property(disableMoxy, true);

        context.register(new JacksonJaxbJsonProvider(mapper, DEFAULT_ANNOTATIONS), MessageBodyReader.class, MessageBodyWriter.class);
        return true;
    }
}
