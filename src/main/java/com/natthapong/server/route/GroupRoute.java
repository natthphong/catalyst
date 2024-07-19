package com.natthapong.server.route;

import com.natthapong.server.handler.HttpHandler;
import com.natthapong.server.middleware.Middleware;

import java.util.List;

public interface GroupRoute {


    GroupRoute middleware(Middleware middleware);

    GroupRoute middlewareAsync(Middleware middleware);

    GroupRoute afterResponse(Middleware middleware);

    GroupRoute afterResponseAsync(Middleware middleware);

    RouteDefinition get(String path, HttpHandler handler);

    RouteDefinition post(String path, HttpHandler handler);

    RouteDefinition put(String path, HttpHandler handler);

    RouteDefinition delete(String path, HttpHandler handler);


    List<Middleware> getMiddlewares();

    List<Middleware> getAfterResponseMiddlewares();

}
