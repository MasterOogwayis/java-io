package com.zsw.demo.nio;

import lombok.SneakyThrows;

import java.util.concurrent.TimeUnit;

/**
 * @author ZhangShaowei on 2019/6/17 13:25
 **/
public class NioServer {

    @SneakyThrows
    public static void main(String[] args) {
        Thread thread = new Thread(new ServerSockektChannelHandler(8088));

        thread.start();

        TimeUnit.SECONDS.sleep(30);


        thread.interrupt();

    }

}
