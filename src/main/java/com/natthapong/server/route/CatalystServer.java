package com.natthapong.server.route;

import com.natthapong.server.handler.HttpHandler;
import com.natthapong.server.middleware.Middleware;
import com.natthapong.server.route.impl.CatalystServerImpl;
import com.natthapong.utils.Httpenum.HttpMethod;

public interface CatalystServer {
    static CatalystServer init() {
        return new CatalystServerImpl();
    }

    void listen(int port);

    RouteDefinition addRouteDefinition(HttpMethod method, String path, HttpHandler handler);

    RouteDefinition addRouteDefinition(HttpMethod method, String path, GroupRoute groupRoute, HttpHandler handler);

    GroupRoute group(String prefix);

    CatalystServer middleware(Middleware middleware);
    CatalystServer middlewareAsync(Middleware middleware);

    CatalystServer afterResponse(Middleware middleware);
    CatalystServer afterResponseAsync(Middleware middleware);

    RouteDefinition get(String path, HttpHandler handler);
    RouteDefinition post(String path, HttpHandler handler);
    RouteDefinition put(String path, HttpHandler handler);
    RouteDefinition delete(String path, HttpHandler handler);
}
