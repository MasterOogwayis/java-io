package com.zsw.demo.nio;

import lombok.SneakyThrows;

/**
 * @author ZhangShaowei on 2019/6/13 10:49
 **/
public class NioServer {

    @SneakyThrows
    public static void main(String[] args) {

        Thread t = new Thread(new NioServerSocketHandler(8088));

        t.start();
        Thread.sleep(30 * 1000);

        t.interrupt();
    }

}
