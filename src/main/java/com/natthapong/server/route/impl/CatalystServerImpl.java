package com.natthapong.server.route.impl;

import com.natthapong.server.handler.HttpHandler;
import com.natthapong.server.middleware.Middleware;
import com.natthapong.server.middleware.MiddlewareChain;
import com.natthapong.server.middleware.impl.AfterResponseChainImpl;
import com.natthapong.server.middleware.impl.MiddlewareChainImpl;
import com.natthapong.server.model.AppRequest;
import com.natthapong.server.model.AppResponse;
import com.natthapong.server.model.response.ServerDefaultResponse;
import com.natthapong.server.route.CatalystServer;
import com.natthapong.server.route.GroupRoute;
import com.natthapong.server.route.RouteDefinition;
import com.natthapong.utils.Httpenum.HttpMethod;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CatalystServerImpl implements CatalystServer {

    private final Map<String, RouteDefinitionServer> routes = new HashMap<>();
    private final Map<String, RouteDefinitionServer> routesRegex = new HashMap<>();
    private final GroupRoute groupRoute = new GroupRouteImpl(this, "/");

    @Override
    public void listen(int port) {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {

                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new HttpServerCodec());
                            p.addLast(new HttpObjectAggregator(1048576));
                            p.addLast(new SimpleChannelInboundHandler<FullHttpRequest>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
                                    String uri = req.uri();
                                    String method = req.method().name();
                                    String fullPath = method + ":" + uri;
                                    RouteDefinitionServer rdfs = routes.get(fullPath);
                                    if (rdfs == null) {
                                        for (Map.Entry<String, RouteDefinitionServer> entry : routesRegex.entrySet()) {
                                            String regexFullPath = entry.getKey();
                                            if (fullPath.matches(regexFullPath)) {
                                                rdfs = entry.getValue();
                                            }
                                        }
                                    }

                                    AppResponse response = new AppResponse(ctx);
                                    if (rdfs != null) {
                                        AppRequest request = new AppRequest(req);
                                        List<Middleware> middlewares = new ArrayList<>(rdfs.getGroupRoute().getMiddlewares());
                                        middlewares.addAll(rdfs.getMiddlewares());
                                        MiddlewareChain middlewareChain = new MiddlewareChainImpl(rdfs.getHandler(), middlewares, ctx);

                                        middlewareChain.next(request, response);
                                        List<Middleware> afterResponse = new ArrayList<>(rdfs.getGroupRoute().getAfterResponseMiddlewares());
                                        afterResponse.addAll(rdfs.getAfterResponseMiddlewares());

                                        MiddlewareChain afterResponseChain = new AfterResponseChainImpl(afterResponse, ctx);
                                        System.out.println("afterResponse" + afterResponse.size());
                                        afterResponseChain.next(request, response);
                                    } else {
                                        response.sendJson(ServerDefaultResponse.notFound());
                                    }

                                }
                            });
                        }
                    });

            ChannelFuture f = null;
            try {
                f = b.bind(port).sync();
                System.out.printf("Server started on port %d%n", port);
                f.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }


    @Override
    public RouteDefinition addRouteDefinition(HttpMethod method, String path, HttpHandler handler) {
        return this.addRouteDefinition(method, path, this.groupRoute, handler);
    }

    @Override
    public RouteDefinition addRouteDefinition(HttpMethod method, String path, GroupRoute groupRoute, HttpHandler handler) {
        String methodStr = method.getValue();
        String fullPath = methodStr + ":" + path;
        fullPath = fullPath.replaceAll(":\\w+", "\\\\w+");
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
