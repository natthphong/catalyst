package com.natthapong.server.route.impl;

import com.natthapong.server.handler.HttpHandler;
import com.natthapong.server.middleware.Middleware;
import com.natthapong.server.route.GroupRoute;
import com.natthapong.server.route.RouteDefinition;

import java.util.*;

public class RouteDefinitionServer implements RouteDefinition {
    private final String path;
    private final String method;
    private final String regexPath;
    private final List<String> pathVariable;
    private final HttpHandler handler;
    private final List<Middleware> middlewares;
    private final List<Middleware> afterResponseMiddlewares;
    private final boolean isRegexPath;
    private final GroupRoute groupRoute;

    public RouteDefinitionServer(String regexPath, String path, String method, HttpHandler handler, GroupRoute groupRoute) {
        this.path = path;
        this.method = method;
        this.handler = handler;
        this.groupRoute = groupRoute;
        this.middlewares = new ArrayList<>();
        this.afterResponseMiddlewares = new ArrayList<>();
        this.regexPath = regexPath;
        this.pathVariable = this.getPathVariablesName(path);
        this.isRegexPath = !this.pathVariable.isEmpty();
    }


    private List<String> getPathVariablesName(String path) {
        List<String> pathVariablesName = new ArrayList<>();
        String[] routeSegments = path.split("/");
        for (String routeSegment : routeSegments) {
            if (routeSegment.startsWith(":")) {
                String key = routeSegment.substring(1);
                pathVariablesName.add(key);
            }
        }
        return pathVariablesName;
    }


    @Override
    public RouteDefinition middleware(Middleware middleware) {
        this.middlewares.add(middleware);
        return this;
    }

    @Override
    public RouteDefinition middlewareAsync(Middleware middleware) {
        //TODO
        return this.middleware(middleware);
    }

    @Override
    public RouteDefinition filterAsync(Middleware middleware) {
        //TODO
        return this.middleware(middleware);
    }

    @Override
    public RouteDefinition afterResponse(Middleware middleware) {
        this.afterResponseMiddlewares.add(middleware);
        return this;
    }

    @Override
    public RouteDefinition afterResponseAsync(Middleware middleware) {
        //TODO
        return this.afterResponse(middleware);
    }


    public String getPath() {
        return path;
    }

    public String getMethod() {
        return method;
    }

    public List<String> getPathVariable() {
        return pathVariable;
    }

    public HttpHandler getHandler() {
        return handler;
    }

    public List<Middleware> getMiddlewares() {
        return middlewares;
    }

    public List<Middleware> getAfterResponseMiddlewares() {
        return afterResponseMiddlewares;
    }

    public String getRegexPath() {
        return regexPath;
    }

    public boolean isRegexPath() {
        return isRegexPath;
    }

    public GroupRoute getGroupRoute() {
        return groupRoute;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RouteDefinitionServer that = (RouteDefinitionServer) o;
        return Objects.equals(path, that.path) && Objects.equals(method, that.method) && Objects.equals(pathVariable, that.pathVariable) && Objects.equals(handler, that.handler) && Objects.equals(middlewares, that.middlewares) && Objects.equals(afterResponseMiddlewares, that.afterResponseMiddlewares);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, method, pathVariable, handler, middlewares, afterResponseMiddlewares);
    }

    @Override
    public String toString() {
        return "RouteDefinitionServer{" +
                "path='" + path + '\'' +
                ", method='" + method + '\'' +
                ", pathVariable=" + pathVariable +
                ", handler=" + handler +
                ", middlewares=" + middlewares +
                ", afterResponseMiddlewares=" + afterResponseMiddlewares +
                '}';
    }
}
