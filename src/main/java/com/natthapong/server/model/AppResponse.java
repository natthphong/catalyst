package com.natthapong.server.model;

import com.natthapong.utils.json.JsonHelper;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

import java.io.IOException;

public class AppResponse {
    private ChannelHandlerContext ctx;
    private FullHttpResponse response;
    private volatile boolean committed;

    public AppResponse(ChannelHandlerContext ctx) {
        this.ctx = ctx;
        this.response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        this.committed = false;
    }

    public void setStatus(HttpResponseStatus status) {
        this.response.setStatus(status);
    }

    public void send(String responseText) {
        if (!committed) {
            response.content().writeBytes(Unpooled.copiedBuffer(responseText, io.netty.util.CharsetUtil.UTF_8));
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            committed = true;
        }
    }

    public void sendJson(Object data) throws IOException {
        String json = JsonHelper.objectToJsonString(data);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
        send(json);
    }

    public void send(byte[] body) {
        if (!committed) {
            response.content().writeBytes(body);
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            committed = true;
        }
    }

    public void setContentType(String value) {
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, value);
    }

    public void setHeader(String key, String value) {
        response.headers().set(key, value);
    }

    public FullHttpResponse getResponse() {
        return response;
    }

    public boolean isCommitted() {
        return committed;
    }
}
