package com.natthapong;

import com.natthapong.server.model.CatalystFileUpload;
import com.natthapong.server.route.CatalystServer;



public class Main {
    public static void main(String[] args) {
        CatalystServer server = CatalystServer.init();
        server.get("/health",((req, res) -> {
            res.send("hello");
        }));
        server.post("/upload",(req,res)->{

            CatalystFileUpload file =  req.getFileUpload("file");
            String fileName = file.getFilename();
            byte[] fileContent = file.getBytes();
            System.out.println("Received file name: " + fileName);
//            System.out.println("Received file: \n" + new String(fileContent));

            res.send("File uploaded successfully");
        });





        server.listen(8080);


    }
}



