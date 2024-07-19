package com.natthapong;

import com.natthapong.server.AppServer;
import com.natthapong.server.route.CatalystServer;
import com.natthapong.utils.json.JsonHelper;
import io.netty.handler.codec.http.HttpResponseStatus;


import java.util.List;
import java.util.stream.IntStream;

public class Main {
    public static void main(String[] args) {
        CatalystServer server = CatalystServer.init();

        server.middleware(((req, res, chain) -> {
            System.out.println("hello eiei gu mai chain");
            chain.next(req,res);
        }));
        server.afterResponse(((req, res, chain) -> {
            System.out.println("afterresponse eiei");
            chain.next(req,res);
        }));
        server.get("/", ((req, res) -> res.send("init")));
        server.get("/hello", ((req, res) -> res.send("hello")));

        server.post("/create/:id", ((req, res) -> {
            String body = req.getBody();

            System.out.println("body:" + body);
            res.setStatus(HttpResponseStatus.CREATED);
            res.send("created");
        }));

        server.post("/create/:id/eiei/:hello/ok", ((req, res) -> {
            String body = req.getBody();

            System.out.println("body:" + body);
            res.setStatus(HttpResponseStatus.CREATED);
            res.send("created");
        }));
        server.listen(8080);


    }
}



