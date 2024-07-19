package com.natthapong.server.handler;

import com.natthapong.server.model.AppRequest;
import com.natthapong.server.model.AppResponse;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

@FunctionalInterface
public interface HttpHandler {


    void handle(AppRequest req, AppResponse res);
}
