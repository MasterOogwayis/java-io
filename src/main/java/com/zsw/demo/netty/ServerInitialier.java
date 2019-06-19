package com.zsw.demo.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ZhangShaowei on 2019/6/19 17:33
 **/
@Slf4j
public class ServerInitialier extends ChannelInitializer<Channel> {

    private static final StringDecoder DECODER = new StringDecoder();
    private static final StringEncoder ENCODER = new StringEncoder();

    private static final ServerHandler SERVER_HANDLER = new ServerHandler();

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ch.pipeline()
                .addLast(new DelimiterBasedFrameDecoder(8 * 1024, Delimiters.lineDelimiter()))
                .addLast(DECODER)
                .addLast(ENCODER)
                .addLast(SERVER_HANDLER);
    }
}
