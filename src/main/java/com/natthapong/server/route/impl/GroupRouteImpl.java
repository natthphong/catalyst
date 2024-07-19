/*
 * Copyright (c) 2567. created by natthaphong jaroenpronrpaist.
 */

package com.natthapong.server.route.impl;

import com.natthapong.server.handler.HttpHandler;
import com.natthapong.server.middleware.Middleware;
import com.natthapong.server.route.CatalystServer;
import com.natthapong.server.route.GroupRoute;
import com.natthapong.server.route.RouteDefinition;
import com.natthapong.utils.Httpenum.HttpMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;



public class GroupRouteImpl implements GroupRoute {

    private final CatalystServer catalystServer;
    private final String prefix;
    private final List<Middleware> middlewares;
    private final List<Middleware> afterResponseMiddlewares;

    public GroupRouteImpl(CatalystServer catalystServer, String prefix) {
        this.catalystServer = catalystServer;
        this.prefix = prefix;
        this.middlewares = new ArrayList<>();
        this.afterResponseMiddlewares = new ArrayList<>();
    }

    @Override
    public GroupRoute middleware(Middleware middleware) {
        this.middlewares.add(middleware);
        return this;
    }

    @Override
    public GroupRoute middlewareAsync(Middleware middleware) {
        //TODO
        return this.middleware(middleware);
    }

    @Override
    public GroupRoute afterResponse(Middleware middleware) {
        this.afterResponseMiddlewares.add(middleware);
        return this;
    }

    @Override
    public GroupRoute afterResponseAsync(Middleware middleware) {
        //TODO
        return this.afterResponse(middleware);
    }

    @Override
    public RouteDefinition get(String path, HttpHandler handler) {
        return catalystServer.addRouteDefinition(HttpMethod.GET, path, this, handler);
    }

    @Override
    public RouteDefinition post(String path, HttpHandler handler) {
        return catalystServer.addRouteDefinition(HttpMethod.POST, path, this, handler);
    }

    @Override
    public RouteDefinition put(String path, HttpHandler handler) {
        return catalystServer.addRouteDefinition(HttpMethod.PUT, path, this, handler);
    }

    @Override
    public RouteDefinition delete(String path, HttpHandler handler) {
        return catalystServer.addRouteDefinition(HttpMethod.DELETE, path, this, handler);
    }


    @Override
    public String toString() {
        return "GroupRouteImpl{" +
                "catalystServer=" + catalystServer +
                ", prefix='" + prefix + '\'' +
                ", middlewares=" + middlewares +
                ", afterResponseMiddlewares=" + afterResponseMiddlewares +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupRouteImpl that = (GroupRouteImpl) o;
        return Objects.equals(catalystServer, that.catalystServer) && Objects.equals(prefix, that.prefix) && Objects.equals(middlewares, that.middlewares) && Objects.equals(afterResponseMiddlewares, that.afterResponseMiddlewares);
    }

    @Override
    public int hashCode() {
        return Objects.hash(catalystServer, prefix, middlewares, afterResponseMiddlewares);
    }

    public CatalystServer getCatalystServer() {
        return catalystServer;
    }

    public String getPrefix() {
        return prefix;
    }

    public List<Middleware> getMiddlewares() {
        return middlewares;
    }

    public List<Middleware> getAfterResponseMiddlewares() {
        return afterResponseMiddlewares;
    }
}
