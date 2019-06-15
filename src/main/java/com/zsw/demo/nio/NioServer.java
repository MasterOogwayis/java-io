package com.zsw.demo.nio;

import lombok.SneakyThrows;

import java.util.concurrent.TimeUnit;

/**
 * @author Administrator on 2019/6/15 13:42
 **/
public class NioServer {

    @SneakyThrows
    public static void main(String[] args) {
        Thread t = new Thread(new ServerSocketChannelHandler(8088));

        t.start();

        TimeUnit.SECONDS.sleep(30);

        t.interrupt();
    }

}
