/*
 * (C) Copyright 2019 Nuxeo (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Damon Brown
 */
package org.nuxeo.ecm.automation.http;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.runtime.model.ComponentContext;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.ClientFilter;

public class HTTPClientComponent extends DefaultComponent implements HTTPClientService {

    static final Log log = LogFactory.getLog(HTTPClientComponent.class);

    public static final String EP_CLIENT = "client";

    protected HTTPClientDescriptor desc;

    protected Client client;

    public HTTPClientComponent() {
        super();
    }

    @Override
    public void activate(ComponentContext context) {
        super.activate(context);
        getClient();
    }

    @Override
    public void deactivate(ComponentContext context) {
        super.deactivate(context);
        client = null;
    }

    @Override
    public void registerContribution(Object contribution, String extensionPoint, ComponentInstance contributor) {
        if (EP_CLIENT.equals(extensionPoint)) {
            desc = (HTTPClientDescriptor) contribution;
        }
    }

    @Override
    public Client getClient() {
        if (client == null) {
            ClientConfig cc = new DefaultClientConfig();

            if (desc != null) {
                Map<String, Boolean> feats = cc.getFeatures();
                Map<String, Object> props = cc.getProperties();

                Map<String, String> propCfg = desc.getProperties();
                if (propCfg != null) {
                    for (Entry<String, String> kv : propCfg.entrySet()) {
                        String key = kv.getKey();
                        if (key.contains("Size") || key.contains("Timeout")) {
                            props.put(key, Integer.parseInt(kv.getValue()));
                        } else {
                            props.put(key, Boolean.parseBoolean(kv.getValue()));
                        }
                    }
                }

                propCfg = desc.getFeatures();
                if (propCfg != null) {
                    for (Entry<String, String> kv : propCfg.entrySet()) {
                        feats.put(kv.getKey(), Boolean.parseBoolean(kv.getValue()));
                    }
                }
            }

            client = Client.create(cc);

            if (desc != null) {
                List<String> clsNames = desc.getFilters();
                for (String cls : clsNames) {
                    try {
                        Class<?> filter = Class.forName(cls);
                        client.addFilter((ClientFilter) filter.newInstance());
                    } catch (Exception ex) {
                        log.error("Error loading HTTP client filter: " + cls, ex);
                    }
                }
            }
        }
        return client;
    }

}
