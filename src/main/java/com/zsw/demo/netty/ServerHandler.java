package com.zsw.demo.netty;

import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * @author ZhangShaowei on 2019/9/25 16:00
 **/
@Slf4j
@ChannelHandler.Sharable
class ServerHandler extends SimpleChannelInboundHandler<Person> {

    private int i = 0;


    @Override
    protected void messageReceived(ChannelHandlerContext ctx, Person msg) throws Exception {
        log.info("msg={}, i={}", msg, i++);
        ChannelFuture channelFuture;
        boolean close = false;
        if ("exit".equalsIgnoreCase(msg.getName())) {
            msg.setName("Have a good day.");
            channelFuture = ctx.writeAndFlush(msg);
            close = true;
        } else {
            msg.setName("Echo from server: " + msg);
            channelFuture = ctx.writeAndFlush(msg);
        }

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
