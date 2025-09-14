/*
 * Copyright (c) 2568. created by natthaphong jaroenpronrpaist.
 */

package com.natthapong.server.route.impl;

import com.natthapong.server.core.CatalystHttpParser;
import com.natthapong.server.core.CatalystHttpRequest;
import com.natthapong.server.core.CatalystServerConfig;
import com.natthapong.server.exception.AppRequestException;
import com.natthapong.server.model.AppRequest;
import com.natthapong.server.model.AppResponse;
import com.natthapong.server.model.response.ServerDefaultResponse;
import com.natthapong.utils.Httpenum.HttpContentType;
import com.natthapong.utils.Httpenum.HttpMethod;
import com.natthapong.server.middleware.Middleware;
import com.natthapong.server.middleware.MiddlewareChain;
import com.natthapong.server.middleware.impl.MiddlewareChainImpl;
import com.natthapong.server.middleware.impl.AfterResponseChainImpl;
import com.natthapong.server.route.impl.RouteDefinitionServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiFunction;

import static com.natthapong.server.core.CatalystHttpResponseWriter.*;

public class CatalystSocketServer {

    /**
     * Router lookup function provided by CatalystServerImpl:
     *   (method, path) -> RouteDefinitionServer or null
     */
    private final BiFunction<String, String, RouteDefinitionServer> routeResolver;
    private final CatalystServerConfig config;
    private final ExecutorService workerPool;

    public CatalystSocketServer(BiFunction<String, String, RouteDefinitionServer> routeResolver,
                                CatalystServerConfig config) {
        this.routeResolver = routeResolver;
        this.config = config;
        this.workerPool = new ThreadPoolExecutor(
                config.coreThreads(),
                config.maxThreads(),
                config.keepAliveSeconds(), TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                r -> {
                    Thread t = new Thread(r, "catalyst-http-worker");
                    t.setDaemon(true);
                    return t;
                },
                new ThreadPoolExecutor.AbortPolicy()
        );
    }

    public void start() {
        try (ServerSocket server = new ServerSocket(config.port(), config.backlog())) {
            System.out.printf("Catalyst HTTP server on :%d", config.port());
            while (true) {
                final Socket socket = server.accept();
                if (config.socketReadTimeoutMs() > 0) {
                    try { socket.setSoTimeout(config.socketReadTimeoutMs()); } catch (Exception ignored) {}
                }
                workerPool.execute(() -> handleClient(socket));
            }
        } catch (IOException e) {
            throw new RuntimeException("HTTP server failed: " + e.getMessage(), e);
        }
    }

    private void handleClient(Socket socket) {
        try (socket;
             InputStream in = socket.getInputStream();
             OutputStream out = socket.getOutputStream()) {

            CatalystHttpRequest req = CatalystHttpParser.parse(in, config.maxRequestSize());
            if (req == null) return;

            if ("/favicon.ico".equalsIgnoreCase(req.path)) {
                write(out, 204, "No Content", "", "text/plain");
                return;
            }

            String method = req.method;
            String uri = req.path;
            RouteDefinitionServer rdfs = routeResolver.apply(method, uri);

            String headerType = req.headers.getOrDefault("content-type", "");
            String contentType = headerType != null ? headerType.split(";")[0].trim() : "";

            AppResponse response = new AppResponse(out);
            try {
                if (rdfs != null) {
                    AppRequest request = new AppRequest(req);
                    if (HttpMethod.POST.getValue().equalsIgnoreCase(method)
                            && HttpContentType.MULTIPART_FORM_DATA.getValue().equalsIgnoreCase(contentType)) {
                        request.setModeBodyFile(true);
                    }

                    List<Middleware> mws = new ArrayList<>(rdfs.getGroupRoute().getMiddlewares());
                    mws.addAll(rdfs.getMiddlewares());
                    MiddlewareChain chain = new MiddlewareChainImpl(rdfs.getHandler(), mws);
                    chain.next(request, response);

                    List<Middleware> after = new ArrayList<>(rdfs.getGroupRoute().getAfterResponseMiddlewares());
                    after.addAll(rdfs.getAfterResponseMiddlewares());
                    MiddlewareChain afterChain = new AfterResponseChainImpl(after);
                    afterChain.next(request, response);

                    response.ensureFlushedOrNoContent();
                } else {
                    response.sendJson(ServerDefaultResponse.notFound(), STATUS_NOT_FOUND);
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                response.sendJson(ServerDefaultResponse.internalError(), STATUS_INTERNAL_ERROR);
            }

        } catch (AppRequestException bre) {
            try {
                write(socket.getOutputStream(), 400, "Bad Request", "{\"message\":\"Bad Request\"}", "application/json");
            } catch (IOException ignored) {}
        } catch (Exception e) {
            System.out.println("Error handling request: " + e.getMessage());
        }
    }
}
