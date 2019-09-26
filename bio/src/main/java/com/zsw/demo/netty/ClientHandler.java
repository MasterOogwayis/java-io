package com.zsw.demo.netty;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ZhangShaowei on 2019/9/25 16:01
 **/
@Slf4j
@ChannelHandler.Sharable
class ClientHandler extends SimpleChannelInboundHandler<String> {
    @Override
    protected void messageReceived(ChannelHandlerContext ctx, String msg) throws Exception {
        log.info(msg);
    }
}
