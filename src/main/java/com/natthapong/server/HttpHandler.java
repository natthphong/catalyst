package com.natthapong.server;

import com.natthapong.server.model.AppRequest;
import com.natthapong.server.model.AppResponse;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

@FunctionalInterface
public interface HttpHandler {


    Object handle(AppRequest req, AppResponse res);
}
