package com.zsw.demo.nio;

import lombok.SneakyThrows;

import java.util.concurrent.TimeUnit;

/**
 * @author Administrator on 2019/6/16 18:55
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
