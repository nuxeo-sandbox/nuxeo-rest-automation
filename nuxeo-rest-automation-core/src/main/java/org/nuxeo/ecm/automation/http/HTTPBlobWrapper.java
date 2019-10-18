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

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.impl.blob.BlobWrapper;

import com.sun.jersey.api.client.ClientResponse;

/**
 * @since
 */
public class HTTPBlobWrapper extends BlobWrapper {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    protected final transient ClientResponse response;

    protected boolean error = false;

    protected int status = 0;

    protected String message = null;

    /**
     * 
     */
    public HTTPBlobWrapper(ClientResponse response, Blob blob) {
        super(blob);
        this.response = response;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    
    public ClientResponse getResponse() {
        return response;
    }

}
