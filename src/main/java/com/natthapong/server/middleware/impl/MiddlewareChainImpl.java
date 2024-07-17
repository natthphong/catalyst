package com.natthapong.server.middleware.impl;

import com.natthapong.server.HttpHandler;
import com.natthapong.server.middleware.Middleware;
import com.natthapong.server.middleware.MiddlewareChain;
import com.natthapong.server.model.AppRequest;
import com.natthapong.server.model.AppResponse;
import com.natthapong.utils.json.JsonHelper;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.util.List;

public class MiddlewareChainImpl implements MiddlewareChain {
    private final HttpHandler handler;
    private final List<Middleware> middlewares;
    private final AppResponse response;
    private final ChannelHandlerContext ctx;
    private int currentIndex = -1;

    public MiddlewareChainImpl(HttpHandler handler, List<Middleware> middlewares, AppResponse response, ChannelHandlerContext ctx) {
        this.handler = handler;
        this.middlewares = middlewares;
        this.response = response;
        this.ctx = ctx;
    }

    @Override
    public void next(AppRequest req, AppResponse res) throws IOException {
        if (currentIndex==-1){
            currentIndex++;
            Object responseData = handler.handle(req, res);
            sendResponse(res,responseData);
        }
        // TODO
        if (currentIndex < middlewares.size()) {
            currentIndex++;
            middlewares.get(currentIndex).apply(req, res, this);
        }
    }

    private void sendResponse(AppResponse res,Object responseData) {
        if (!res.isCommitted()) {
            String responseDataStr = JsonHelper.objectToJsonString(responseData);
            res.send(responseDataStr);
        }
    }
}