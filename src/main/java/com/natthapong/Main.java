//package com.natthapong;
//
//import com.natthapong.server.route.CatalystServer;
//import com.natthapong.server.route.GroupRoute;
//import io.netty.handler.codec.http.HttpResponseStatus;
//
//public class Main {
//    public static void main(String[] args) {
//        CatalystServer server = CatalystServer.init();
//
//
//        server.afterResponse(((req, res, chain) -> {
//            System.out.println("afterresponse eiei");
//            chain.next(req, res);
//        }));
//        server.get("/", ((req, res) -> res.send("init")));
//        server.get("/hello", ((req, res) -> res.send("hello")));
//
//        server.middleware(((req, res, chain) -> {
//            System.out.println("hello eiei gu mai chain");
//            chain.next(req, res);
//        }));
//
//        GroupRoute api = server.group("/api/v1");
//
//
//        api.get("/hello", ((req, res) -> res.send("hello from group")));
//
//        api.middleware(((req, res, chain) -> {
//            System.out.println("in group api");
//            chain.next(req, res);
//        }));
//
//        api.afterResponse(((req, res, chain) -> {
//            System.out.println("after Response");
//            chain.next(req, res);
//        }));
//
//        server.listen(8080);
//
//
//    }
//}
//
//
//
