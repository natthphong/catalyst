package com.natthapong.server.model.response;

import java.time.LocalDateTime;

public class ServerDefaultResponse {
    private LocalDateTime timeStamp;

    private String message;

    public ServerDefaultResponse() {
    }

    public ServerDefaultResponse(LocalDateTime timeStamp, String message) {
        this.timeStamp = timeStamp;
        this.message = message;
    }

    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(LocalDateTime timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static ServerDefaultResponse notFound(){
        return new ServerDefaultResponse(LocalDateTime.now(),"path not found");
    }
    @Override
    public String toString() {
        return "ServerDefaultResponse{" +
                "timeStamp=" + timeStamp +
                ", message='" + message + '\'' +
                '}';
    }
}
