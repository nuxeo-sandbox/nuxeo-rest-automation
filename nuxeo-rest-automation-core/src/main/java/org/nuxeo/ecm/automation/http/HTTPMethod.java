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

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;
import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.StatusType;

import org.apache.commons.lang3.StringUtils;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.BlobCollector;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.Blobs;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HttpHeaders;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.core.header.ContentDisposition;

/**
 *
 */
@Operation(id = HTTPMethod.ID, category = Constants.CAT_BLOB, label = "HTTP Method", description = "Invoke a RESTful HTTP Method.")
public class HTTPMethod {

    public static final String ID = "Blob.HTTPMethod";

    @Context
    protected OperationContext ctx;

    @Context
    protected AutomationService automation;

    @Param(name = "method", description = "HTTP Method", required = false, widget = Constants.W_OPTION, values = {
            "GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS", "TRACE" })
    String method = "GET";

    @Param(name = "url", description = "Target URL", required = true)
    protected String url;

    @Param(name = "headerMap", description = "Headers as a Map", required = false)
    protected Properties headerMap;

    @Param(name = "headers", description = "Headers as JSON", required = false)
    protected String headerJSON;

    @Param(name = "paramMap", description = "Query parameters as a Map", required = false)
    protected Properties paramMap;

    @Param(name = "params", description = "Query parameters as JSON", required = false)
    protected String paramJSON;

    @Param(name = "body", description = "Body to send", required = false)
    protected String body;

    @Param(name = "contentType", description = "'Content-Type' request header", required = false)
    protected String contentType;

    @Param(name = "accept", description = "'Accept' header MIME Type", required = false)
    protected String accept = MediaType.WILDCARD;

    @Param(name = "download", description = "Create Blob from response", required = false)
    protected boolean download = false;

    protected Client getClient() {
        ClientConfig cc = new DefaultClientConfig();
        cc.getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, true);
        Client client = Client.create(cc);
        return client;
    }

    @OperationMethod(collector = BlobCollector.class)
    public Blob run(DocumentModel doc) throws Exception {
        BlobHolder bh = doc.getAdapter(BlobHolder.class);
        if (bh == null) {
            return null;
        }
        return run(bh.getBlob());
    }

    @OperationMethod
    public Blob run() throws IOException, OperationException {
        return run((Blob) null);
    }

    @OperationMethod(collector = BlobCollector.class)
    public Blob run(Blob blob) throws IOException, OperationException {
        WebResource resource = getClient().resource(url);
        addParams(resource, paramMap, paramJSON);

        WebResource.Builder builder = resource.accept(accept);
        addHeaders(builder, headerMap, headerJSON);

        if ("PUT".equals(method) || "POST".equals(method)) {
            if (body != null) {
                if (contentType == null) {
                    contentType = "text/plain";
                }
                if (contentType != null) {
                    builder.type(contentType);
                }
                builder.entity(body);
            } else if (blob != null) {
                if (contentType == null && blob.getMimeType() != null) {
                    contentType = blob.getMimeType();
                }
                if (contentType != null) {
                    builder.type(contentType);
                }
                builder.entity(blob.getStream());
            }
        }

        ClientResponse response = null;
        Blob result = null;
        try {
            response = builder.method(method, ClientResponse.class);

            if (download) {
                MultivaluedMap<String, String> respHeaders = response.getHeaders();
                InputStream data = response.getEntityInputStream();
                result = Blobs.createBlob(data);

                if (response.getType() != null) {
                    result.setMimeType(response.getType().toString());
                }

                String encoding = respHeaders.getFirst(HttpHeaders.CONTENT_ENCODING);
                if (encoding != null) {
                    result.setEncoding(encoding);
                }

                String filename = null;
                if (respHeaders.containsKey(HttpHeaders.CONTENT_DISPOSITION)) {
                    String disp = respHeaders.getFirst(HttpHeaders.CONTENT_DISPOSITION);
                    ContentDisposition cdisp = new ContentDisposition(disp);
                    filename = cdisp.getFileName();
                }

                if (StringUtils.isBlank(filename)) {
                    // extracts file name from URL
                    filename = url.substring(url.lastIndexOf("/") + 1, url.length());
                }
                if (StringUtils.isBlank(filename)) {
                    filename = "DownloadedFile-" + UUID.randomUUID().toString();
                }
                result.setFilename(filename);
            } else {
                result = Blobs.createBlob(response.getEntity(String.class), "text/plain", "UTF-8");
            }

            ctx.put("http_error", false);
        } catch (UniformInterfaceException ufe) {
            ctx.put("http_error", true);
            ctx.put("http_errorMessage", ufe.getMessage());

            throw new NuxeoException(ufe.getMessage(), ufe, ufe.getResponse().getStatus());

        } catch (Exception e) {
            ctx.put("http_error", true);
            ctx.put("http_errorMessage", e.getMessage());

            throw new NuxeoException(e.getMessage(), e);

        } finally {
            // Cleanup
            if (response != null) {
                // Set context environment
                StatusType status = response.getStatusInfo();
                ctx.put("http_status", status.getStatusCode());
                ctx.put("http_statusMessage", status.getReasonPhrase());
                ctx.put("http_lastModified", response.getLastModified());

                response.close();
            }
        }

        return result;
    }

    protected void addHeaders(WebResource.Builder inHttp, Properties inProps, String inJsonStr) throws IOException {

        if (inProps != null) {
            inProps.stringPropertyNames().stream().forEach(h -> inHttp.header(h, inProps.getProperty(h)));
        }

        if (StringUtils.isNotBlank(inJsonStr)) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(inJsonStr);
            Iterator<String> it = rootNode.fieldNames();
            while (it.hasNext()) {
                String oneHeader = it.next();
                inHttp.header(oneHeader, rootNode.get(oneHeader).textValue());
            }
        }
    }

    protected void addParams(WebResource inHttp, Properties inProps, String inJsonStr) throws IOException {

        if (inProps != null) {
            inProps.stringPropertyNames().stream().forEach(h -> inHttp.queryParam(h, inProps.getProperty(h)));
        }

        if (StringUtils.isNotBlank(inJsonStr)) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(inJsonStr);
            Iterator<String> it = rootNode.fieldNames();
            while (it.hasNext()) {
                String oneHeader = it.next();
                inHttp.queryParam(oneHeader, rootNode.get(oneHeader).textValue());
            }
        }
    }
}
