package com.zsw.demo.netty;

import com.zsw.demo.serializer.MarshallingCodecFactory;
import com.zsw.demo.serializer.ProtostuffDecoder;
import com.zsw.demo.serializer.ProtostuffEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ZhangShaowei on 2019/9/25 16:00
 **/
@Slf4j
public class NettyServer {


    public static void main(String[] args) {
        start("127.0.0.1", 8080);
    }


    private static void start(String host, int port) {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();


        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .childHandler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            channel.pipeline()
                                    // 基于分隔符的解码器
//                                    .addLast(new DelimiterBasedFrameDecoder(8 * 1024, Delimiters.lineDelimiter()))
//                                    .addLast(new DelimiterBasedFrameDecoder(8 * 1024, Unpooled.copiedBuffer("$_".getBytes())))
                                    // 定长解码，缓存超出的半包，等待下个包到达后进行拼包
//                                    .addLast(new FixedLengthFrameDecoder(20))
                                    // 定长解码，超出长度报错
//                                    .addLast(new LineBasedFrameDecoder(100))
                                    // 以 {}或[] 解析长度的 json字符串 ByteBuf
//                                    .addLast(new JsonObjectDecoder())
//                                    .addLast(new StringDecoder())
//                                    .addLast(new StringEncoder())
                                    // jdk 序列化
//                                    .addLast(new ObjectDecoder(1024 * 1024, ClassResolvers.weakCachingResolver(this.getClass().getClassLoader())))
//                                    .addLast(new ObjectEncoder())
                                    // google protostuff
                                    .addLast(new ProtostuffDecoder())
                                    .addLast(new ProtostuffEncoder())
                                    // protobuf
//                                    .addLast(new ProtobufVarint32FrameDecoder())
//                                    .addLast(new ProtobufDecoder())
//                                    .addLast(new ProtobufVarint32LengthFieldPrepender())
//                                    .addLast(new ProtobufEncoder())
                                    // jboss-marshalling
//                                    .addLast(MarshallingCodecFactory.buildMarshallingDecoder())
//                                    .addLast(MarshallingCodecFactory.buildMarshallingEncoder())
                                    .addLast(new ServerHandler());
                        }
                    });
            ChannelFuture sync = serverBootstrap.bind(host, port).sync();
            sync.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }


}
