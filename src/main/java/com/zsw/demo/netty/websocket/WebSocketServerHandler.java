package com.zsw.demo.netty.websocket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Objects;

import static io.netty.handler.codec.http.HttpHeaderUtil.isKeepAlive;
import static io.netty.handler.codec.http.HttpHeaderUtil.setContentLength;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.LOCKED;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static io.netty.util.CharsetUtil.UTF_8;

/**
 * @author ZhangShaowei on 2019/10/9 10:25
 **/
@Slf4j
@ChannelHandler.Sharable
public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {

    private String address;

    private int port;

    private static final String UPGRADE = "websocket";

    private WebSocketServerHandshaker handshaker;

    public WebSocketServerHandler(String address, int port) {
        this.address = address;
        this.port = port;
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }


    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request) {
        // HTTP 解码失败则返回 HTTP 异常
        if (request.decoderResult().isFailure() || !UPGRADE.equalsIgnoreCase((String) request.headers().get("Upgrade"))) {
            setHttpResponse(ctx, request, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
            return;
        }

        // 构造握手响应返回
        WebSocketServerHandshakerFactory factory
                = new WebSocketServerHandshakerFactory(
                "ws://" + address + ":" + port + "/websocket", null, false);
        this.handshaker = factory.newHandshaker(request);
        if (Objects.isNull(this.handshaker)) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            // 这里会将 WebSocket 相关的编解码类动态的添加到 pipeline
            this.handshaker.handshake(ctx.channel(), request);
        }

    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        Channel channel = ctx.channel();
        // 判断是否是关闭连接的指令
        if (frame instanceof CloseWebSocketFrame) {
            this.handshaker.close(channel, (CloseWebSocketFrame) frame);
            return;
        }

        // 判断是否是 ping 消息
        if (frame instanceof PingWebSocketFrame) {
            channel.write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }

        // demo 支持 文本消息
        if (!(frame instanceof TextWebSocketFrame)) {
            throw new UnsupportedOperationException(frame.getClass().getName() + " frame types not supported");
        }

        // 应答消息
        TextWebSocketFrame textWebSocketFrame = (TextWebSocketFrame) frame;
        String text = textWebSocketFrame.text();
        log.info("{} received {}", channel, text);
        channel.write(new TextWebSocketFrame(text + ", welcome to WebSocket Server, it is " + new Date() + " now"));
    }


    private void setHttpResponse(ChannelHandlerContext ctx, FullHttpRequest request, DefaultFullHttpResponse response) {
        if (response.status().code() != 200) {
            ByteBuf byteBuf = Unpooled.copiedBuffer(response.status().toString(), UTF_8);
            response.content().writeBytes(byteBuf);
            byteBuf.release();
            setContentLength(response, response.content().readableBytes());
        }

        // 如果不是 keep-alive 则关闭连接
        ChannelFuture channelFuture = ctx.channel().writeAndFlush(response);
        if (!isKeepAlive(request) || response.status().code() != 200) {
            channelFuture.addListener(ChannelFutureListener.CLOSE);
        }


    }


}
