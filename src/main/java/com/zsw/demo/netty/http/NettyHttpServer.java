package com.zsw.demo.netty.http;

import com.zsw.demo.netty.http.handler.HttpFileServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ZhangShaowei on 2019/9/27 13:21
 **/
@Slf4j
public class NettyHttpServer {

    private static final String rootPath = "/data/netty";

    public static void main(String[] args) {
        start("192.168.1.191", 8080);
    }


    private static void start(String host, int port) {
        EventLoopGroup boosGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(boosGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    // http的解码器
                                    .addLast("http-decoder", new HttpRequestDecoder())
                                    // ObjectAggregator解码器，作用是他会把多个消息转换为单一的FullHttpRequest或者FullHttpResponse
                                    .addLast("http-aggregator", new HttpObjectAggregator(65536))
                                    // http的编码器
                                    .addLast("http-encoder", new HttpResponseEncoder())
                                    // chunked 主要作用是支持异步发送的码流（大文件传输），但不专用过多的内存，防止java内存溢出
                                    .addLast("http-chunked", new ChunkedWriteHandler())
                                    .addLast("fileServerHandler", new HttpFileServerHandler(rootPath));
                        }
                    });

                ChannelFuture channelFuture = serverBootstrap.bind(host, port).sync();
                log.info("服务器已启动，host={}, port={}", host, port);
                channelFuture.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            log.error("error", e);
        } finally {
            boosGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }



    }

}
