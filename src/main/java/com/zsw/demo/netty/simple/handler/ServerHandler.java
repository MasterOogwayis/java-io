package com.zsw.demo.netty.simple.handler;

import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Objects;

/**
 * @author ZhangShaowei on 2019/9/25 16:00
 **/
@Slf4j
@ChannelHandler.Sharable
public class ServerHandler extends SimpleChannelInboundHandler<String> {

    private static final String ENTER = "\r\n";

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        ctx.writeAndFlush("Welcome to server, it is " + new Date() + " now." + ENTER);
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, String message) throws Exception {
        log.info(message);
        ChannelFuture channelFuture;
        boolean close = false;
        String data;
        if (Objects.isNull(message) || "".equalsIgnoreCase(message)) {
            data = "Please type something." + ENTER;
        } else if ("exit".equalsIgnoreCase(message)) {
            data = "Have a good day." + ENTER;
            close = true;
        } else {
            data = "Echo from server: " + message + ENTER;
        }
        channelFuture = ctx.writeAndFlush(data);
        if (close) {
            channelFuture.addListener(ChannelFutureListener.CLOSE);
            channelFuture.channel().close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        super.exceptionCaught(ctx, cause);
    }

    //    @Override
//    protected void messageReceived(ChannelHandlerContext ctx, String msg) throws Exception {
//        log.info("msg={}, i={}", msg, i++);
//        ChannelFuture channelFuture;
//        boolean close = false;
//        if ("exit".equalsIgnoreCase(msg)) {
//            channelFuture = ctx.writeAndFlush("Have a good day.\r\n");
//            close = true;
//        } else if (msg == null || msg.length() == 0) {
//            channelFuture = ctx.writeAndFlush("Please type something.\r\n");
//        } else {
//            channelFuture = ctx.writeAndFlush("Echo from server: " + msg + "\r\n");
//        }
//
//        if (close) {
//            channelFuture.addListener(ChannelFutureListener.CLOSE);
//            channelFuture.channel().close();
//        }
//    }
}
