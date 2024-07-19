package com.natthapong.server.middleware.impl;

import com.natthapong.server.handler.HttpHandler;
import com.natthapong.server.middleware.Middleware;
import com.natthapong.server.middleware.MiddlewareChain;
import com.natthapong.server.model.AppRequest;
import com.natthapong.server.model.AppResponse;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.util.List;

public class MiddlewareChainImpl implements MiddlewareChain {
    private final HttpHandler handler;
    private final List<Middleware> middlewares;
    private final ChannelHandlerContext ctx;
    private int currentIndex = -1;

    public MiddlewareChainImpl(HttpHandler handler, List<Middleware> middlewares, ChannelHandlerContext ctx) {
        this.handler = handler;
        this.middlewares = middlewares;
        this.ctx = ctx;
    }

    @Override
    public void next(AppRequest req, AppResponse res) throws IOException {
        currentIndex++;
        if (currentIndex < middlewares.size()) {
            middlewares.get(currentIndex).apply(req, res, this);
        }else{
            handler.handle(req, res);
            sendResponse(res);
        }
    }

    private void sendResponse(AppResponse res) {
        if (!res.isCommitted()) {
            res.send(new byte[0]);
        }
    }
}