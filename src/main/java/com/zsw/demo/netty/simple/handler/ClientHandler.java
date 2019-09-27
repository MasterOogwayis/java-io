package com.zsw.demo.netty.simple.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @author ZhangShaowei on 2019/9/25 16:01
 **/
@Slf4j
@ChannelHandler.Sharable
public class ClientHandler extends SimpleChannelInboundHandler<Map<String, Object>> {
    @Override
    protected void messageReceived(ChannelHandlerContext ctx, Map<String, Object> msg) throws Exception {
        log.info("msg={}", msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        ctx.close();
    }
}
