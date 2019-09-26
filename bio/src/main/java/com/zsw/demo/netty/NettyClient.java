package com.zsw.demo.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author ZhangShaowei on 2019/9/25 16:00
 **/
@Slf4j
public class NettyClient {

    public static void main(String[] args) {
        start("127.0.0.1", 8080);
    }


    private static void start(String host, int port) {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            channel.pipeline()
                                    .addLast(new DelimiterBasedFrameDecoder(4 * 1024, Delimiters.lineDelimiter()))
                                    .addLast(new StringDecoder())
                                    .addLast(new StringEncoder())
                                    .addLast(new ClientHandler());
                        }
                    });
            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
            Channel channel = channelFuture.channel();

            while (true) {
                String line = reader.readLine();
                if (null == line || line.length() == 0) {
                    continue;
                }
                if ("exit".equalsIgnoreCase(line)) {
                    channel.closeFuture().sync();
                    break;
                }
                channel.writeAndFlush(line + "\r\n");
            }
            channelFuture.sync();
        } catch (InterruptedException e) {
            log.error("客户端已被终止...", e);
        } catch (IOException e) {
            log.error("error", e);
        }


    }

}
