package com.natthapong.server.model;

import com.natthapong.utils.json.JsonHelper;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class AppRequest {

    private FullHttpRequest request;

    private AtomicBoolean bodyReady = new AtomicBoolean(false);
    private String body;
    private String method;
    private final HttpPostRequestDecoder decoder;
    private final Map<String,FileUpload> fileUpload = new HashMap<>();
    private final Map<String,String> attributeMap = new HashMap<>();
    private boolean modeBodyFile;

    private Map<String, List<String>> headers = new HashMap<>();
    public AppRequest(FullHttpRequest request) {
        this.request = request;
        this.decoder = new HttpPostRequestDecoder(request);
        this.modeBodyFile = false;
        this.method = request.method().name();
    }

    public boolean isModeBodyFile() {
        return modeBodyFile;
    }

    public FileUpload getFileUpload(String name) {
        return fileUpload.get(name);
    }


    public Map<String, FileUpload> getFileUpload() {
        return fileUpload;
    }

    public  String getAttributeMap(String name) {
        return attributeMap.get(name);
    }
    public Map<String, String> getAttributeMap() {
        return attributeMap;
    }

    public void setModeBodyFile(boolean modeBodyFile) {
        this.modeBodyFile = modeBodyFile;
        if (this.modeBodyFile){
            try {
                while (decoder.hasNext()) {
                    InterfaceHttpData data = decoder.next();
                    if (data != null) {
                        switch (data.getHttpDataType()) {
                            case FileUpload:
                                FileUpload fileUpload = (FileUpload) data;
                                String key = fileUpload.getName();
                                this.fileUpload.put(key,fileUpload.copy());
                                break;
                            case Attribute:
                                Attribute attribute = (Attribute) data;
                                String attributeName = attribute.getName();
                                String value = null;
                                try {
                                    value = new String(attribute.get(), StandardCharsets.UTF_8);
                                } catch (IOException e) {
                                    System.out.println("error:"+e.getMessage());
                                }
                                this.attributeMap.put(attributeName,value);
                                break;
                            default:
                                break;
                        }
                    }
                }
            }catch (HttpPostRequestDecoder.EndOfDataDecoderException e){
                e.printStackTrace();
                System.err.println("End of multipart data reached or data is incomplete.");
            }catch (Exception e) {
                System.err.println("An error occurred while processing multipart data.");
                e.printStackTrace();
            } finally {
                decoder.destroy();
            }
        }
    }

    public String getMethod() {
        return this.method;
    }

    public String getBody() {
        if (!bodyReady.get() && !modeBodyFile) {
            body = request.content().toString(io.netty.util.CharsetUtil.UTF_8);
            bodyReady.set(true);
        }
        return body;
    }

    public <T> T getBodyForObject(Class<T> tClass) {
        if (!bodyReady.get()&& !modeBodyFile) {
            body = request.content().toString(io.netty.util.CharsetUtil.UTF_8);
            bodyReady.set(true);
        }
        return JsonHelper.jsonStringToObject(body, tClass);

    }

    public String getPath() {
        return request.uri();
    }

    public Map<String, List<String>> getHeaders() {
        if (headers.isEmpty()){
            headers  = request.headers().entries().stream().collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
        }
        return headers;
    }
}
