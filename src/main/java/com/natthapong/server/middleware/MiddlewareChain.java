package com.natthapong.server.middleware;

import com.natthapong.server.model.AppRequest;
import com.natthapong.server.model.AppResponse;

import java.io.IOException;

public interface MiddlewareChain {
    void next(AppRequest req, AppResponse res) throws IOException;

}
