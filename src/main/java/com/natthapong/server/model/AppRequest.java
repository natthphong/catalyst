package com.natthapong.server.model;

import com.natthapong.utils.json.JsonHelper;
import io.netty.handler.codec.http.FullHttpRequest;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class AppRequest {

    private FullHttpRequest request;

    private AtomicBoolean bodyReady = new AtomicBoolean(false);
    private String body;

    public AppRequest(FullHttpRequest request) {
        this.request = request;
        new Thread(() -> {
            body = request.content().toString(io.netty.util.CharsetUtil.UTF_8);
            bodyReady.set(true);
        }
        ).start();
    }

    public String getMethod() {
        return request.method().name();
    }

    public String getBody() {
        if (bodyReady.get()) {
            return body;
        }
        return getBody();
    }

    public <T> T getBodyForObject(Class<T> tClass) {
        if (bodyReady.get()) {
            return JsonHelper.jsonStringToObject(body, tClass);
        }
        return getBodyForObject(tClass);
    }

    public String getPath() {
        return request.uri();
    }

    public Map<String, List<String>> getHeaders() {
        return request.headers().entries().stream().collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
    }
}
