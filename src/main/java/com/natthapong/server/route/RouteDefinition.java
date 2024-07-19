package com.natthapong.server.route;

import com.natthapong.server.middleware.Middleware;

public interface RouteDefinition {


    RouteDefinition middleware(Middleware middleware);
    RouteDefinition middlewareAsync(Middleware middleware);
    RouteDefinition filterAsync(Middleware middleware);
    RouteDefinition afterResponse(Middleware middleware);
    RouteDefinition afterResponseAsync(Middleware middleware);



}
