package com.natthapong.utils.Httpenum;

public enum HttpContentType {
    APPLICATION_JSON("application/json"),
    APPLICATION_XML("application/xml"),
    TEXT_PLAIN("text/plain"),
    TEXT_HTML("text/html"),
    IMAGE_JPEG("image/jpeg"),
    IMAGE_PNG("image/png"),
    APPLICATION_PDF("application/pdf"),
    APPLICATION_OCTET_STREAM("application/octet-stream"),
    MULTIPART_FORM_DATA("multipart/form-data"),
    APPLICATION_X_WWW_FORM_URLENCODED("application/x-www-form-urlencoded");
    private final String value;

    HttpContentType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
