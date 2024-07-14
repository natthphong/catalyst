package com.natthapong.server.middleware;

import com.natthapong.server.model.AppRequest;
import com.natthapong.server.model.AppResponse;

import java.io.IOException;

@FunctionalInterface
public interface Middleware {
    void apply(AppRequest req, AppResponse res, MiddlewareChain chain) throws IOException;
}