package com.zsw.demo.netty.file;

import io.netty.channel.*;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author ZhangShaowei on 2019/10/9 14:23
 **/
@Slf4j
public class FileServerHandler extends SimpleChannelInboundHandler<String> {
    private static final String ENTER = "\r\n";

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, String msg) throws Exception {
        File file = Paths.get(msg).toFile();
        if (!file.exists() || file.isDirectory()) {
            ctx.writeAndFlush("File '" + msg + "' not found!" + ENTER);
            return;
        }
        ctx.writeAndFlush(file + " " + file.length() + ENTER);
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
        DefaultFileRegion defaultFileRegion = new DefaultFileRegion(randomAccessFile.getChannel(), 0, randomAccessFile.length());
        ctx.write(defaultFileRegion);
        ctx.writeAndFlush(ENTER);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        cause.printStackTrace();
    }
}
