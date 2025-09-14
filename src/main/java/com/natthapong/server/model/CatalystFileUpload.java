package com.natthapong.server.model;

public class CatalystFileUpload {
    private final String name;
    private final String filename;
    private final String contentType;
    private final byte[] bytes;

    public CatalystFileUpload(String name, String filename, String contentType, byte[] bytes) {
        this.name = name;
        this.filename = filename;
        this.contentType = contentType;
        this.bytes = bytes;
    }

    public String getName() { return name; }
    public String getFilename() { return filename; }
    public String getContentType() { return contentType; }
    public byte[] getBytes() { return bytes; }
    public long length() { return bytes == null ? 0 : bytes.length; }
}
