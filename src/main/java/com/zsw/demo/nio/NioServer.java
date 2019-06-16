package com.zsw.demo.nio;

import lombok.SneakyThrows;

import java.util.concurrent.TimeUnit;

/**
 * MappedByteBuffer，用于表示内存映射文件
 *
 * @author Administrator on 2019/6/15 19:44
 **/
public class NioServer {

    @SneakyThrows
    public static void main(String[] args) {
        Thread thread = new Thread(new ServerSocketChannelHandler(8088));
        thread.start();

        TimeUnit.SECONDS.sleep(30);
        thread.interrupt();
    }

}
