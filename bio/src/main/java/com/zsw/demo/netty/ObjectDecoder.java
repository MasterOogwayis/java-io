package com.zsw.demo.netty;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

/**
 * @author ZhangShaowei on 2019/9/25 17:14
 **/
public class ObjectDecoder extends MessageToMessageDecoder<String> {
    private Gson gson = new GsonBuilder().create();

    @Override
    protected void decode(ChannelHandlerContext ctx, String msg, List<Object> out) throws Exception {
        out.add(this.gson.fromJson(msg, Person.class));
    }
}
