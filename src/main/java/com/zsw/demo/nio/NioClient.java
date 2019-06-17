package com.zsw.demo.nio;

import java.net.InetSocketAddress;

/**
 * @author ZhangShaowei on 2019/6/17 13:26
 **/
public class NioClient {

    public static void main(String[] args) {
        Thread thread = new Thread(new SocketChannelHandler(new InetSocketAddress("127.0.0.1", 8088), "Hello World!"));

        thread.start();


    }

}
