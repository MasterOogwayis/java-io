package com.zsw.demo.netty.websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ZhangShaowei on 2019/10/9 9:55
 **/
@Slf4j
public class WebSocketServer {

    public static void main(String[] args) {
        start("localhost", 8080);
    }

    private static void start(String address, int port) {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel sc) throws Exception {
                            sc.pipeline()
                                    // 将请求和应答消息编码或解码为 HTTP 消息
                                    .addLast("http-codec", new HttpServerCodec())
                                    // 将 HTTP 消息的多个部分组合成一条完整的 HTTP 消息
                                    .addLast("aggregator", new HttpObjectAggregator(2 << 15))
                                    // 向客户端发送 HTML5 文件，支持浏览器和服务端进行 websocket 通信
                                    .addLast("http-chunked", new ChunkedWriteHandler())
                                    .addLast("handler", new WebSocketServerHandler(address, port));
                        }
                    });

            Channel channel = serverBootstrap.bind(address, port).sync().channel();
            log.info("服务器已启动，address={},port={}", address, port);
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("服务器异常", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }


    }


}
