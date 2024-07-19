package com.natthapong.server;

import com.natthapong.server.exception.AppRequestException;
import com.natthapong.server.handler.HttpHandler;
import com.natthapong.server.middleware.Middleware;
import com.natthapong.server.middleware.MiddlewareChain;
import com.natthapong.server.middleware.impl.MiddlewareChainImpl;
import com.natthapong.server.model.AppRequest;
import com.natthapong.server.model.AppResponse;
import com.natthapong.server.model.response.ServerDefaultResponse;
import com.natthapong.utils.Httpenum.HttpMethod;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Deprecated
public class AppServer {

    private final Map<String, RouteDefinition> routes = new HashMap<>();

    private final List<RouteDefinition> routeDefinitions = new ArrayList<>();

    private final List<Middleware> middlewares = new ArrayList<>();

    private final Map<String, List<Middleware>> middlewareGroup = new HashMap<>();

    public AppServer() {
    }


    public AppServer middleware(Middleware middleware) {
        middlewares.add(middleware);
        return this;
    }

    private void pushRoute (String method,String path, HttpHandler handler){
        RouteDefinition route = new RouteDefinition(handler, HttpMethod.POST.getValue(), path);
        if (route.pathVariable.isEmpty()){
            routes.put(HttpMethod.POST.getValue() + path, route);
        }else{
            routeDefinitions.add(route);
        }
    }
    public AppServer get(String path, HttpHandler handler) {

        return this;
    }

    public AppServer post(String path, HttpHandler handler) {

        return this;
    }

//    public RouteGroup group(String prefix) {
//        return new RouteGroup(prefix, this);
//    }

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
                                    //TODO URi method with class route
                                    System.out.println( method);
                                    System.out.println(uri);
//                                    RouteDefinition routeDef = routes.get(method + uri);
                                    AppResponse response = new AppResponse(ctx);
//                                    if (routeDef != null) {
//                                        AppRequest request = new AppRequest(req);
//                                        MiddlewareChain chain = new MiddlewareChainImpl(routeDef.handler, middlewares, ctx);
//                                        new MiddlewareExecutor(middlewares, chain)
//                                                .execute(request, response);
//                                    } else {
//                                        response.sendJson(ServerDefaultResponse.notFound());
//                                    }
                                    response.sendJson(ServerDefaultResponse.notFound());
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

//    public AppServer afterResponse(Middleware middleware) {
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


    private static class RouteDefinition {
        protected String path;
        protected String method;
        private final  Pattern pattern;

        protected List<String> pathVariable;
        protected HttpHandler handler;
        protected List<Middleware> middlewares;
        protected List<Middleware> afterResponseMiddlewares;

        public RouteDefinition(HttpHandler handler, String method, String path) {
            this.handler = handler;
            this.middlewares = new ArrayList<>();
            this.afterResponseMiddlewares = new ArrayList<>();
            this.pattern = createPattern(path);
            this.pathVariable = extractPathName(path);
        }

//        private Map<String, String> parseQueryParams(String query) {
//            //TODO can speed than this
//             query = query.split("\\?").length > 1
//                     ? query.split("\\?")[1]
//                     : "";
//            Map<String, String> queryParams = new HashMap<>();
//            String[] pairs = query.split("&");
//            for (String pair : pairs) {
//                String[] keyValue = pair.split("=");
//                String key = keyValue[0];
//                String value = keyValue.length > 1 ? keyValue[1] : "";
//                queryParams.put(key, value);
//            }
//            return queryParams;
//        }
        public Map<String, String> checkPathUrl(String requestPath) {
            Matcher matcher = pattern.matcher(requestPath);
            Map<String, String> pathVariables = new HashMap<>();
            if (matcher.matches()) {
                for (int i = 1; i <= matcher.groupCount(); i++) {
                    String name = pathVariable.get(i-1);
                    pathVariables.put(name, matcher.group(i));
                }
            }else{
                throw new AppRequestException(404,"path not found");
            }
            return pathVariables;
        }


        private List<String> extractPathName(String path) {
            List<String> groupNames = new ArrayList<>();
            Pattern pattern = Pattern.compile("\\{([^/]+)\\}");
            Matcher matcher = pattern.matcher(path);
            while (matcher.find()) {
                groupNames.add(matcher.group(1));
            }
            return groupNames;
        }

        private Pattern createPattern(String path) {
            String regex = path.replaceAll("\\{([^/]+)\\}", "(?<$1>[^/]+)");
            return Pattern.compile(regex);
        }

    }
}

