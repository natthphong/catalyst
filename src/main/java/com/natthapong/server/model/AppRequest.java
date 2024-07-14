package com.natthapong.server.model;

import io.netty.handler.codec.http.FullHttpRequest;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AppRequest {

    private FullHttpRequest request;

    public AppRequest(FullHttpRequest request) {
        this.request = request;
    }

    public String getMethod() {
        return request.method().name();
    }

    public String getBody() {
        return request.content().toString(io.netty.util.CharsetUtil.UTF_8);
    }

    public String getPath() {
        return request.uri();
    }

    public Map<String, List<String>> getHeaders() {
        return request.headers().entries().stream().collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
    }
}
