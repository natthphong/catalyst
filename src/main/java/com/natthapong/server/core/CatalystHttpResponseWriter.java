/*
 * Copyright (c) 2568. created by natthaphong jaroenpronrpaist.
 */

package com.natthapong.server.core;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class CatalystHttpResponseWriter {
    public static final int STATUS_OK = 200;
    public static final int STATUS_NO_CONTENT = 204;
    public static final int STATUS_NOT_FOUND = 404;
    public static final int STATUS_INTERNAL_ERROR = 500;
    public static final int STATUS_BAD_REQUEST = 400;

    public static void write(OutputStream out, int status, String reason, String body, String contentType) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        Map<String,String> headers = new LinkedHashMap<>();
        headers.put("Content-Type", contentType == null ? "application/json; charset=utf-8" : contentType);
        headers.put("Content-Length", String.valueOf(bytes.length));
        headers.put("Connection", "close");
        writeHead(out, status, reason, headers);
        out.write(bytes);
        out.flush();
    }

    public static void writeHead(OutputStream out, int status, String reason, Map<String,String> headers) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 ").append(status).append(' ').append(reason).append("\r\n");
        for (Map.Entry<String,String> e : headers.entrySet()) {
            sb.append(e.getKey()).append(": ").append(e.getValue()).append("\r\n");
        }
        sb.append("\r\n");
        out.write(sb.toString().getBytes(StandardCharsets.US_ASCII));
    }
}

