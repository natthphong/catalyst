//package com.natthapong;
//
//import com.natthapong.server.route.CatalystServer;
//import com.natthapong.server.route.GroupRoute;
//import io.netty.buffer.ByteBuf;
//import io.netty.handler.codec.http.HttpResponseStatus;
//import io.netty.handler.codec.http.multipart.FileUpload;
//
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//
//public class Main {
//    public static void main(String[] args) {
//        CatalystServer server = CatalystServer.init();
//
//        server.post("/upload",(req,res)->{
//
//            FileUpload file =  req.getFileUpload("file");
//            String fileName = file.getFilename();
//            ByteBuf fileContent = file.content();
//            try {
//                byte[] tmp =file.get();
//                System.out.println("Received file: \n" + new String(tmp));
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//
//
//        });
//
//
//
//
//
//        server.listen(8080);
//
//
//    }
//}
//
//
//
