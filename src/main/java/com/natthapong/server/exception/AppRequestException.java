package com.natthapong.server.exception;

public class AppRequestException extends RuntimeException{

    private int code;

    public AppRequestException(int code ,String message) {
        super(message);
        this.code = code;
    }
    public AppRequestException(String message) {
        super(message);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
