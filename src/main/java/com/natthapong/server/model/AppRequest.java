package com.natthapong.server.model;

import com.natthapong.server.core.CatalystHttpRequest;
import com.natthapong.utils.json.JsonHelper;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Netty-free AppRequest backed by CatalystHttpRequest.
 */
public class AppRequest {

    private final CatalystHttpRequest request;
    private final AtomicBoolean bodyReady = new AtomicBoolean(false);
    private String body;
    private final String method;

    // multipart support
    private boolean modeBodyFile;
    private final Map<String, CatalystFileUpload> fileUpload = new HashMap<>();
    private final Map<String, String> attributeMap = new HashMap<>();

    /** Headers as {@code Map<String, List<String>>} (compat). */

    private Map<String, List<String>> headers = new HashMap<>();

    public AppRequest(CatalystHttpRequest request) {
        this.request = request;
        this.method = request.method;
        this.modeBodyFile = false;
    }

    // ---------------- multipart toggle & parsing ----------------

    public void setModeBodyFile(boolean modeBodyFile) {
        this.modeBodyFile = modeBodyFile;
        if (!modeBodyFile) return;

        String ct = request.headers.getOrDefault("content-type", "");
        String boundary = extractBoundary(ct);
        if (boundary == null || boundary.isEmpty() || request.body == null) {
            return; // nothing to parse
        }
        parseMultipart(request.body, boundary, fileUpload, attributeMap);
    }

    public boolean isModeBodyFile() {
        return modeBodyFile;
    }

    // ---------------- getters (compat with your existing code) ----------------

    public String getMethod() {
        return this.method;
    }

    public String getBody() {
        if (!bodyReady.get() && !modeBodyFile) {
            body = new String(request.body == null ? new byte[0] : request.body, StandardCharsets.UTF_8);
            bodyReady.set(true);
        }
        return body;
    }

    public <T> T getBodyForObject(Class<T> tClass) {
        if (!bodyReady.get() && !modeBodyFile) {
            body = new String(request.body == null ? new byte[0] : request.body, StandardCharsets.UTF_8);
            bodyReady.set(true);
        }
        return JsonHelper.jsonStringToObject(body, tClass);
    }

    /** @return the HTTP path including the query string (rawPath). */
    public String getPath() {
        // rawPath contains original target (may include query)
        return request.rawPath;
    }

    /** Headers as {@code Map<String, List<String>>} (compat).
     *  @return headers grouped as a list per name
     */
    public Map<String, List<String>> getHeaders() {
        if (headers.isEmpty()) {
            headers = request.headers.entrySet().stream()
                    .collect(Collectors.toMap(
                            e -> e.getKey(),                       // keep original/lower-case key from Catalyst
                            e -> Collections.singletonList(e.getValue()),
                            (a, b) -> a,
                            LinkedHashMap::new
                    ));
        }
        return headers;
    }

    // --------------- multipart accessors (Netty FileUpload replacements) ---------------

    public CatalystFileUpload getFileUpload(String name) {
        return fileUpload.get(name);
    }

    public Map<String, CatalystFileUpload> getFileUpload() {
        return fileUpload;
    }

    public String getAttributeMap(String name) {
        return attributeMap.get(name);
    }

    public Map<String, String> getAttributeMap() {
        return attributeMap;
    }

    // ---------------- helpers: multipart parsing ----------------

    private static String extractBoundary(String contentType) {
        // e.g. "multipart/form-data; boundary=----WebKitFormBoundaryX"
        if (contentType == null) return null;
        String[] parts = contentType.split(";");
        for (String p : parts) {
            String s = p.trim();
            if (s.toLowerCase(Locale.ROOT).startsWith("boundary=")) {
                String b = s.substring("boundary=".length());
                if (b.startsWith("\"") && b.endsWith("\"") && b.length() >= 2) {
                    b = b.substring(1, b.length() - 1);
                }
                return b;
            }
        }
        return null;
    }

    /**
     * Minimal multipart/form-data parser.
     * - Stores small parts in memory (suitable for typical JSON + small files).
     * - Populates fileUpload map and attributeMap.
     */
    private static void parseMultipart(byte[] body, String boundary,
                                       Map<String, CatalystFileUpload> fileMap,
                                       Map<String, String> attrMap) {
        String dashBoundary = "--" + boundary;
        String endBoundary = dashBoundary + "--";
        // Decode whole body once (simple approach)
        String all = new String(body, StandardCharsets.ISO_8859_1);
        int pos = 0;

        while (true) {
            int start = all.indexOf(dashBoundary, pos);
            if (start < 0) break;
            start += dashBoundary.length();
            if (start + 2 <= all.length() && all.startsWith("--", start)) {
                // reached final boundary
                break;
            }
            // consume CRLF after boundary
            if (start + 2 <= all.length() && all.startsWith("\r\n", start)) {
                start += 2;
            }

            // headers block
            int headersEnd = all.indexOf("\r\n\r\n", start);
            if (headersEnd < 0) break;
            String headersBlock = all.substring(start, headersEnd);
            Map<String, String> partHeaders = parsePartHeaders(headersBlock);

            int partDataStart = headersEnd + 4;

            // find next boundary
            int nextBoundary = all.indexOf("\r\n" + dashBoundary, partDataStart);
            if (nextBoundary < 0) {
                nextBoundary = all.indexOf(endBoundary, partDataStart);
                if (nextBoundary < 0) {
                    nextBoundary = all.length();
                }
            }
            // part data is [partDataStart, nextBoundary)
            byte[] partBytes = all.substring(partDataStart, nextBoundary).getBytes(StandardCharsets.ISO_8859_1);

            // parse Content-Disposition
            String cd = partHeaders.getOrDefault("content-disposition", "");
            Map<String, String> disp = parseContentDisposition(cd);
            String name = disp.get("name");
            String filename = disp.get("filename");
            String contentType = partHeaders.getOrDefault("content-type", "application/octet-stream");

            if (filename != null && !filename.isEmpty()) {
                // file field
                CatalystFileUpload fu = new CatalystFileUpload(
                        name == null ? "" : name,
                        filename,
                        contentType,
                        partBytes
                );
                fileMap.put(name, fu);
            } else if (name != null) {
                // attribute (text)
                String value = new String(partBytes, StandardCharsets.UTF_8);
                // trim trailing CRLF if present (conservative)
                if (value.endsWith("\r\n")) value = value.substring(0, value.length() - 2);
                attrMap.put(name, value);
            }

            // move pos to after this boundary CRLF
            int boundaryPos = all.indexOf(dashBoundary, partDataStart);
            if (boundaryPos < 0) break;
            pos = boundaryPos;
        }
    }

    private static Map<String, String> parsePartHeaders(String headersBlock) {
        Map<String, String> map = new LinkedHashMap<>();
        String[] lines = headersBlock.split("\r\n");
        for (String line : lines) {
            int i = line.indexOf(':');
            if (i > 0) {
                String k = line.substring(0, i).trim().toLowerCase(Locale.ROOT);
                String v = line.substring(i + 1).trim();
                map.put(k, v);
            }
        }
        return map;
    }

    private static Map<String, String> parseContentDisposition(String cd) {
        Map<String, String> res = new HashMap<>();
        // example: form-data; name="file"; filename="a.txt"
        String[] parts = cd.split(";");
        for (String raw : parts) {
            String p = raw.trim();
            int eq = p.indexOf('=');
            if (eq > 0) {
                String key = p.substring(0, eq).trim().toLowerCase(Locale.ROOT);
                String val = p.substring(eq + 1).trim();
                if (val.startsWith("\"") && val.endsWith("\"") && val.length() >= 2) {
                    val = val.substring(1, val.length() - 1);
                }
                res.put(key, val);
            } else {
                // first token is usually "form-data"
                res.putIfAbsent(p.toLowerCase(Locale.ROOT), "");
            }
        }
        return res;
    }
}
