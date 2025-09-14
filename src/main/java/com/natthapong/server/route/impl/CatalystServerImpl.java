package com.natthapong.server.route.impl;

import com.natthapong.server.core.CatalystServerConfig;

import com.natthapong.server.handler.HttpHandler;
import com.natthapong.server.middleware.Middleware;
import com.natthapong.server.middleware.impl.AfterResponseChainImpl;
import com.natthapong.server.middleware.impl.MiddlewareChainImpl;
import com.natthapong.server.route.CatalystServer;
import com.natthapong.server.route.GroupRoute;
import com.natthapong.server.route.RouteDefinition;
import com.natthapong.utils.Httpenum.HttpMethod;

import java.util.HashMap;
import java.util.Map;

public class CatalystServerImpl implements CatalystServer {

    private final Map<String, RouteDefinitionServer> routes = new HashMap<>();
    private final Map<String, RouteDefinitionServer> routesRegex = new HashMap<>();
    private final GroupRoute groupRoute = new GroupRouteImpl(this, "/");
    private CatalystServerConfig config;

    public CatalystServerImpl() {}

    public CatalystServerImpl(CatalystServerConfig config) {
        this.config = config;
    }

    public CatalystServerImpl config(CatalystServerConfig config) {
        this.config = config;
        return this;
    }

    /** Internal resolver used by CatalystSocketServer. */
    private RouteDefinitionServer resolve(String method, String path) {
        String fullPath = method + ":" + path;
        RouteDefinitionServer rdfs = routes.get(fullPath);
        if (rdfs != null) return rdfs;
        for (Map.Entry<String, RouteDefinitionServer> e : routesRegex.entrySet()) {
            if (fullPath.matches(e.getKey())) return e.getValue();
        }
        return null;
    }

    @Override
    public void listen(int port) {
        if (config == null) {
            config = new CatalystServerConfig() {
                @Override public int port() { return port; }
            };
        } else {
            CatalystServerConfig base = config;
            config = new CatalystServerConfig() {
                @Override public int port() { return port; }
                @Override public int backlog() { return base.backlog(); }
                @Override public int maxRequestSize() { return base.maxRequestSize(); }
                @Override public int coreThreads() { return base.coreThreads(); }
                @Override public int maxThreads() { return base.maxThreads(); }
                @Override public long keepAliveSeconds() { return base.keepAliveSeconds(); }
                @Override public int socketReadTimeoutMs() { return base.socketReadTimeoutMs(); }
            };
        }

        CatalystSocketServer server = new CatalystSocketServer(this::resolve, config);
        server.start();
    }

    @Override
    public RouteDefinition addRouteDefinition(HttpMethod method, String path, HttpHandler handler) {
        return this.addRouteDefinition(method, path, this.groupRoute, handler);
    }

    @Override
    public RouteDefinition addRouteDefinition(HttpMethod method, String path, GroupRoute groupRoute, HttpHandler handler) {
        String methodStr = method.getValue();
        String fullPath = (methodStr + ":" + path).replaceAll(":\\w+", "\\\\w+");
        RouteDefinitionServer route = new RouteDefinitionServer(fullPath, path, methodStr, handler, groupRoute);
        if (route.isRegexPath()) {
            routesRegex.put(fullPath, route);
        } else {
            routes.put(fullPath, route);
        }
        return route;
    }

    @Override
    public GroupRoute group(String prefix) {
        return new GroupRouteImpl(this, prefix);
    }

    @Override
    public CatalystServer middleware(Middleware middleware) {
        this.groupRoute.middleware(middleware);
        return this;
    }

    @Override
    public CatalystServer middlewareAsync(Middleware middleware) {
        this.groupRoute.middlewareAsync(middleware);
        return this;
    }

    @Override
    public CatalystServer afterResponse(Middleware middleware) {
        this.groupRoute.afterResponse(middleware);
        return this;
    }

    @Override
    public CatalystServer afterResponseAsync(Middleware middleware) {
        this.groupRoute.afterResponseAsync(middleware);
        return this;
    }

    @Override
    public RouteDefinition get(String path, HttpHandler handler) {
        return this.addRouteDefinition(HttpMethod.GET, path, handler);
    }

    @Override
    public RouteDefinition post(String path, HttpHandler handler) {
        return this.addRouteDefinition(HttpMethod.POST, path, handler);
    }

    @Override
    public RouteDefinition put(String path, HttpHandler handler) {
        return this.addRouteDefinition(HttpMethod.PUT, path, handler);
    }

    @Override
    public RouteDefinition delete(String path, HttpHandler handler) {
        return this.addRouteDefinition(HttpMethod.DELETE, path, handler);
    }
}
