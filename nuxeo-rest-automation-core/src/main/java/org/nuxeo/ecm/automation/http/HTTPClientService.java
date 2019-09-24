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

import com.sun.jersey.api.client.Client;

/**
 * This service prepares the HTTP Client for reuse within the system
 *
 * @since 10.10
 */
public interface HTTPClientService {

    /**
     * Creates and returns the Jersey HTTP Client implementation
     *
     * @return the created client
     * @since 10.10
     */
    Client getClient();


}
