package com.zsw.demo.netty.udp;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import static io.netty.util.CharsetUtil.UTF_8;

/**
 * @author ZhangShaowei on 2019/10/9 13:56
 **/
@Slf4j
public class UdpClient {

    public static void main(String[] args) {
        start(8080);
    }

    public static void start(int port) {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup)
                    .channel(NioDatagramChannel.class)
                    .handler(new UdpClientHandler());
            Channel channel = bootstrap.bind(0).sync().channel();
            // 向网段内所有机器广播 udp 消息
            channel.writeAndFlush(
                    new DatagramPacket(
                            Unpooled.copiedBuffer("Hello World!", UTF_8),
                            new InetSocketAddress("255.255.255.255", port)
                    )
            ).sync();
            if (!channel.closeFuture().await(TimeUnit.SECONDS.toMillis(15))) {
                log.error("访问超时");
            }
        } catch (InterruptedException e) {
            log.error("服务器异常", e);
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }


}
