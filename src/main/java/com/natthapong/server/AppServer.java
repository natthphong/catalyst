package com.natthapong.server;

import com.natthapong.server.middleware.Middleware;
import com.natthapong.server.middleware.MiddlewareChain;
import com.natthapong.server.middleware.impl.MiddlewareChainImpl;
import com.natthapong.server.model.AppRequest;
import com.natthapong.server.model.AppResponse;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppServer {
    private final Map<String, RouteDefinition> routes = new HashMap<>();
    private final List<Middleware> middlewares = new ArrayList<>();


    public AppServer() {}


    public AppServer middleware(Middleware middleware) {
        middlewares.add(middleware);
        return this;
    }

    public AppServer get(String path, HttpHandler handler) {
        routes.put("GET" + path, new RouteDefinition(handler, new ArrayList<>(), new ArrayList<>()));
        return this;
    }

    public AppServer post(String path, HttpHandler handler) {
        routes.put("POST" + path, new RouteDefinition(handler, new ArrayList<>(), new ArrayList<>()));
        return this;
    }

//    public RouteGroup group(String prefix) {
//        return new RouteGroup(prefix, this);
//    }

    public void listen(int port){
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
                                    System.out.println("method"  + method);
                                    RouteDefinition routeDef = routes.get(method + uri);
                                    if (routeDef != null) {
                                        System.out.println("found");
                                        AppRequest request = new AppRequest(req);
                                        AppResponse response = new AppResponse(ctx);
                                        MiddlewareChain chain = new MiddlewareChainImpl(routeDef.handler, routeDef.afterResponseMiddlewares, response, ctx);
                                        new MiddlewareExecutor(middlewares, chain)
                                                .execute(request, response);
                                    } else {
                                        System.out.println("not found");
                                        sendNotFound(ctx);
                                    }
                                }

                                private void sendNotFound(ChannelHandlerContext ctx) {
                                    FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
                                    ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
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

//    public AppServer afterResponse(String path, Middleware middleware) {
//        String key = "GET" + path;
//        if (routes.containsKey(key)) {
//            routes.get(key).afterResponseMiddlewares.add(middleware);
//        } else {
//            routes.put(key, new RouteDefinition(null, new ArrayList<>(), new ArrayList<>()));
//            routes.get(key).afterResponseMiddlewares.add(middleware);
//        }
//        return this;
//    }
//
//    public AppServer afterResponsePost(String path, Middleware middleware) {
//        String key = "POST" + path;
//        if (routes.containsKey(key)) {
//            routes.get(key).afterResponseMiddlewares.add(middleware);
//        } else {
//            routes.put(key, new RouteDefinition(null, new ArrayList<>(), new ArrayList<>()));
//            routes.get(key).afterResponseMiddlewares.add(middleware);
//        }
//        return this;
//    }



    private static class MiddlewareExecutor {
        private final List<Middleware> middlewares;
        private final MiddlewareChain chain;
        private int currentIndex = -1;

        public MiddlewareExecutor(List<Middleware> middlewares, MiddlewareChain chain) {
            this.middlewares = middlewares;
            this.chain = chain;
        }

        public void execute(AppRequest req, AppResponse res) throws IOException {
            currentIndex++;
            if (currentIndex < middlewares.size()) {
                middlewares.get(currentIndex).apply(req, res, this::execute);
            } else {
                chain.next(req, res);
            }
        }
    }
}

class RouteDefinition {
   protected HttpHandler handler;
    protected List<Middleware> middlewares;
    protected List<Middleware> afterResponseMiddlewares;

    public RouteDefinition(HttpHandler handler, List<Middleware> middlewares, List<Middleware> afterResponseMiddlewares) {
        this.handler = handler;
        this.middlewares = middlewares;
        this.afterResponseMiddlewares = afterResponseMiddlewares;
    }
}