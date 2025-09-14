package com.natthapong.server.middleware.impl;

import com.natthapong.server.middleware.Middleware;
import com.natthapong.server.middleware.MiddlewareChain;
import com.natthapong.server.model.AppRequest;
import com.natthapong.server.model.AppResponse;

import java.io.IOException;
import java.util.List;

public class AfterResponseChainImpl implements MiddlewareChain {
    private final List<Middleware> afterResponse;
    private int currentIndex = -1;

    public AfterResponseChainImpl(List<Middleware> afterResponse) {
        this.afterResponse = afterResponse;
    }

    @Override
    public void next(AppRequest req, AppResponse res) throws IOException {
        currentIndex++;
        if (currentIndex < afterResponse.size()) {
            afterResponse.get(currentIndex).apply(req, res, this);
        }
    }
}
