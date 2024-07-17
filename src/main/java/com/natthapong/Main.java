//package com.natthapong;
//
//import com.natthapong.server.AppServer;
//
//public class Main {
//    public static void main(String[] args) {
//        AppServer app = new AppServer();
//        app.middleware((req, res, chain) -> {
//            System.out.println("Request received: " + req.getPath());
//            chain.next(req, res);
//        });
//        app.get("/hello",((req, res) -> {
//            System.out.println("hello");
//            return "HELLO";
//        }));
//        app.listen(8080);
//    }
//}
//
//
