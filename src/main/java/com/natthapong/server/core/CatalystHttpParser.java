/*
 * Copyright (c) 2568. created by natthaphong jaroenpronrpaist.
 */

package com.natthapong.server.core;

import com.natthapong.server.exception.AppRequestException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CatalystHttpParser {

    public static CatalystHttpRequest parse(InputStream in, int maxRequestSize) throws IOException {
        BufferedInputStream bin = new BufferedInputStream(in);
        bin.mark(maxRequestSize);

        String requestLine = readLine(bin);
        if (requestLine == null || requestLine.isEmpty()) return null;

        String[] parts = requestLine.split(" ");
        if (parts.length < 3) throw new AppRequestException("Invalid request line");
        String method = parts[0];
        String target = parts[1];
        String version = parts[2];

        Map<String,String> headers = new LinkedHashMap<>();
        while (true) {
            String line = readLine(bin);
            if (line == null || line.isEmpty()) break;
            int idx = line.indexOf(':');
            if (idx > 0) {
                String k = line.substring(0, idx).trim().toLowerCase(Locale.ROOT);
                String v = line.substring(idx + 1).trim();
                headers.put(k, v);
            }
        }

        int contentLength = 0;
        String cl = headers.get("content-length");
        if (cl != null && !cl.isEmpty()) {
            try { contentLength = Integer.parseInt(cl); } catch (NumberFormatException ignored) {}
        }
        if (contentLength < 0 || contentLength > maxRequestSize) {
            throw new AppRequestException("Body too large");
        }

        byte[] body = new byte[contentLength];
        int read = 0;
        while (read < contentLength) {
            int r = bin.read(body, read, contentLength - read);
            if (r == -1) break;
            read += r;
        }

        String path = target;
        String query = "";
        int q = target.indexOf('?');
        if (q >= 0) {
            path = target.substring(0, q);
            query = target.substring(q + 1);
        }

        return new CatalystHttpRequest(method, path, target, query, version, headers, body);
    }

    private static String readLine(BufferedInputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int prev = -1;
        while (true) {
            int b = in.read();
            if (b == -1) {
                if (baos.size() == 0) return null;
                break;
            }
            if (b == '\n') {
                // trim trailing \r
                break;
            }
            baos.write(b);
            prev = b;
        }
        byte[] arr = baos.toByteArray();
        int len = arr.length;
        if (len > 0 && arr[len - 1] == '\r') len--;
        return new String(arr, 0, len, StandardCharsets.US_ASCII);
    }
}
