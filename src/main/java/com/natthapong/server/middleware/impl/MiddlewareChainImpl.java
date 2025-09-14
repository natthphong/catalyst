package com.natthapong.server.middleware.impl;

import com.natthapong.server.handler.HttpHandler;
import com.natthapong.server.middleware.Middleware;
import com.natthapong.server.middleware.MiddlewareChain;
import com.natthapong.server.model.AppRequest;
import com.natthapong.server.model.AppResponse;

import java.io.IOException;
import java.util.List;

public class MiddlewareChainImpl implements MiddlewareChain {
    private final HttpHandler handler;
    private final List<Middleware> middlewares;
    private int currentIndex = -1;

    public MiddlewareChainImpl(HttpHandler handler, List<Middleware> middlewares) {
        this.handler = handler;
        this.middlewares = middlewares;
    }

    @Override
    public void next(AppRequest req, AppResponse res) throws IOException {
        currentIndex++;
        if (currentIndex < middlewares.size()) {
            middlewares.get(currentIndex).apply(req, res, this);
        } else {
            handler.handle(req, res);
            res.ensureFlushedOrNoContent();
        }
    }
}
