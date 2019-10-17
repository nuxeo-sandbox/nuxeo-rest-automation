# Nuxeo REST Automation

[![Build Status](https://qa.nuxeo.org/jenkins/buildStatus/icon?job=Sandbox/sandbox_nuxeo-rest-automation-master)](https://qa.nuxeo.org/jenkins/view/Sandbox/job/Sandbox/job/sandbox_nuxeo-rest-automation-master/)

Automation support for all standard HTTP methods.

## Build and Install

Build with maven (at least 3.3)

```
mvn clean install
```
> Package built here: `nuxeo-rest-automation-package/target`

> Install with `nuxeoctl mp-install <package>`

## Use

## Blob.HTTPMethod

The method takes a document, blob, or no input.  The input blob is used as the body of the request.  The content type will be automatically set from the blob, if available.

* `method` - The HTTP Method to use
* `url` - The URL to invoke
* `headerMap` - The headers as a property map
* `headers` - The headers as a JSON string
* `paramMap` - The parameters as a property map
* `params` - The parameters as a JSON string
* `body` - The body as a string
* `contentType` - the content MIME type
* `accept` - the accept MIME type
* `download` - true or false, create a blob from the response


## Configure (nuxeo.conf)

Optionally set system-wide properties for the Jersey REST client.

```
  <extension point="client" target="org.nuxeo.ecm.automation.http.HTTPClientService">
    <client>
      <property key="com.sun.jersey.client.property.followRedirects">true</property>
      <property key="com.sun.jersey.client.property.bufferResponseEntityOnException">false</property>
      <feature key="com.sun.jersey.config.feature.Formatted">false</feature>
      <feature key="com.sun.jersey.config.feature.DisableXmlSecurity">false</feature>
    </client>
  </extension>
```

## Support

**These features are sand-boxed and not yet part of the Nuxeo Production platform.**

These solutions are provided for inspiration and we encourage customers to use them as code samples and learning resources.

This is a moving project (no API maintenance, no deprecation process, etc.) If any of these solutions are found to be useful for the Nuxeo Platform in general, they will be integrated directly into platform, not maintained here.

## Licensing

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)

## About Nuxeo

Nuxeo dramatically improves how content-based applications are built, managed and deployed, making customers more agile, innovative and successful. Nuxeo provides a next generation, enterprise ready platform for building traditional and cutting-edge content oriented applications. Combining a powerful application development environment with SaaS-based tools and a modular architecture, the Nuxeo Platform and Products provide clear business value to some of the most recognizable brands including Verizon, Electronic Arts, Sharp, FICO, the U.S. Navy, and Boeing. Nuxeo is headquartered in New York and Paris.

More information is available at [www.nuxeo.com](http://www.nuxeo.com).

