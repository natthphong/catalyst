package com.natthapong.server.middleware.impl;

import com.natthapong.server.middleware.Middleware;
import com.natthapong.server.middleware.MiddlewareChain;
import com.natthapong.server.model.AppRequest;
import com.natthapong.server.model.AppResponse;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.util.List;

public class AfterResponseChainImpl implements MiddlewareChain {
    private final List<Middleware> afterResponse;
    private final ChannelHandlerContext ctx;
    private int currentIndex = -1;

    public AfterResponseChainImpl(List<Middleware> afterResponse, ChannelHandlerContext ctx) {
        this.afterResponse = afterResponse;
        this.ctx = ctx;
    }

    @Override
    public void next(AppRequest req, AppResponse res) throws IOException {
        currentIndex++;
        if (currentIndex < afterResponse.size()) {
            currentIndex++;
            afterResponse.get(currentIndex).apply(req, res, this);
        }
    }

}