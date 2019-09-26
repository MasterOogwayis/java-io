package com.zsw.demo.serializer;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;


/**
 * @author ZhangShaowei on 2019/9/26 15:14
 **/
public class ProtostuffDecoder extends LengthFieldBasedFrameDecoder {

    public ProtostuffDecoder() {
        super(1024, 4, 4);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        try {
            byte[] dstBytes = new byte[in.readableBytes()];
            //in.getBytes(in.readerIndex(), dstBytes);
            //切记这里一定要用readBytes，不能用getBytes，否则会导致readIndex不能向后移动，从而导致netty did not read anything but decoded a message.错误
            in.readBytes(dstBytes, 0, in.readableBytes());
            return ProtostuffSerializer.deserialize(dstBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }
}

