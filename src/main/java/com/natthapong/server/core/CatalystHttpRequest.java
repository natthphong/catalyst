/*
 * Copyright (c) 2568. created by natthaphong jaroenpronrpaist.
 */

package com.natthapong.server.core;

import java.util.Arrays;
import java.util.Map;

public class CatalystHttpRequest {
    public final String method;
    public final String path;       // path without query
    public final String rawPath;    // original target, may include query
    public final String query;      // without '?', may be ""
    public final String httpVersion;
    public final Map<String, String> headers; // lower-cased keys
    public final byte[] body;

    public CatalystHttpRequest(
            String method,
            String path,
            String rawPath,
            String query,
            String httpVersion,
            Map<String, String> headers,
            byte[] body
    ) {
        this.method = method;
        this.path = path;
        this.rawPath = rawPath;
        this.query = query;
        this.httpVersion = httpVersion;
        this.headers = headers;
        this.body = body;
    }


    public CatalystHttpRequest(
            String method,
            String path,
            String rawPath,
            String query,
            String httpVersion,
            Map<String, String> headers
    ) {
        this(method, path, rawPath, query, httpVersion, headers, null);
    }
    public CatalystHttpRequest(
            String method,
            String path,
            String rawPath,
            String query,
            String httpVersion
    ) {
        this(method, path, rawPath, query, httpVersion, null, null);
    }

    public CatalystHttpRequest(
            String method,
            String path,
            String rawPath,
            String query
    ) {
        this(method, path, rawPath, query, null, null, null);
    }

    public CatalystHttpRequest(
            String method,
            String path,
            String rawPath
    ) {
        this(method, path, rawPath, "", null, null, null);
    }

    public CatalystHttpRequest(
            String method,
            String path
    ) {
        this(method, path, path, "", null, null, null);
    }




    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getRawPath() {
        return rawPath;
    }

    public String getQuery() {
        return query;
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public byte[] getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "CatalystHttpRequest{" +
                "method='" + method + '\'' +
                ", path='" + path + '\'' +
                ", rawPath='" + rawPath + '\'' +
                ", query='" + query + '\'' +
                ", httpVersion='" + httpVersion + '\'' +
                ", headers=" + headers +
                ", body=" + Arrays.toString(body) +
                '}';
    }
}