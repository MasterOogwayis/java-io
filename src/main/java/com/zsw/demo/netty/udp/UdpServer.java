package com.zsw.demo.netty.udp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ZhangShaowei on 2019/10/9 13:43
 **/
@Slf4j
public class UdpServer {

    public static void main(String[] args) {
        start("localhost", 8080);
    }

    public static void start(String address, int port) {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup)
                    .channel(NioDatagramChannel.class)
                    // 相比于 TCP 通信，UDP 不存在客户端和服务端的时机连接，所以不需要为连接设置 handler
                    .handler(new UdpServerHandler());
            bootstrap.bind(port).sync().channel().closeFuture().await();
        } catch (InterruptedException e) {
            log.error("服务器异常", e);
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }


}
