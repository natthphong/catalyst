package com.natthapong.server.model;

import com.natthapong.server.core.CatalystHttpResponseWriter;
import com.natthapong.utils.Httpenum.HttpStatus;
import com.natthapong.utils.json.JsonHelper;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Netty-free AppResponse that writes HTTP/1.1 to an OutputStream.
 */
public class AppResponse {

    private final OutputStream out;
    private final Map<String, String> headers = new LinkedHashMap<>();
    private int status = 200;
    private boolean committed = false;
    private boolean headWritten = false;

    public AppResponse(OutputStream out) {
        this.out = out;
        headers.put("Content-Type", "application/json; charset=utf-8");
        headers.put("Connection", "close");
    }

    // ---------- status / headers ----------

    public void setStatus(int status) {
        if (committed) return;
        this.status = status;
    }

    public void setContentType(String value) {
        if (committed) return;
        headers.put("Content-Type", value);
    }

    public void setHeader(String key, String value) {
        if (committed) return;
        headers.put(key, value);
    }

    public boolean isCommitted() {
        return committed;
    }

    // ---------- send ----------

    public void send(String responseText) {
        byte[] data = responseText.getBytes(StandardCharsets.UTF_8);
        send(data);
    }

    public void send(byte[] body) {
        if (committed) return;
        try {
            headers.put("Content-Length", String.valueOf(body.length));
            writeHeadIfNeeded();
            out.write(body);
            out.flush();
            committed = true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendJson(Object data, int statusCode) {
        this.status = statusCode;
        String json = (data instanceof String) ? (String) data : JsonHelper.objectToJsonString(data);
        if (json == null) json = "";
        setContentType("application/json; charset=utf-8");
        send(json);
    }
    public void sendJson(Object data, HttpStatus status) {
        this.status = status.getCode();
        String json = (data instanceof String) ? (String) data : JsonHelper.objectToJsonString(data);
        if (json == null) json = "";
        setContentType("application/json; charset=utf-8");
        send(json);
    }
    public void sendJson(Object data) {
        String json = (data instanceof String) ? (String) data : JsonHelper.objectToJsonString(data);
        if (json == null) json = "";
        setContentType("application/json; charset=utf-8");
        send(json);
    }

    /** Called by server when handler wrote nothing. */
    public void ensureFlushedOrNoContent() {
        if (!committed) {
            try {
                CatalystHttpResponseWriter.write(out, 204, "No Content", "", headers.get("Content-Type"));
                committed = true;
            } catch (IOException ignored) {}
        }
    }

    // ---------- internal ----------

    private void writeHeadIfNeeded() throws IOException {
        if (headWritten) return;
        CatalystHttpResponseWriter.writeHead(out, status, statusText(status), headers);
        headWritten = true;
    }

    private static String statusText(int s) {
        switch (s) {
            case 200: return "OK";
            case 201: return "Created";
            case 202: return "Accepted";
            case 204: return "No Content";
            case 400: return "Bad Request";
            case 401: return "Unauthorized";
            case 403: return "Forbidden";
            case 404: return "Not Found";
            case 409: return "Conflict";
            case 422: return "Unprocessable Entity";
            case 500: return "Internal Server Error";
            default:  return "OK";
        }
    }
}
