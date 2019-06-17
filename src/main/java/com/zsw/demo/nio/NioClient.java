package com.zsw.demo.nio;

import java.net.InetSocketAddress;

/**
 * @author Administrator on 2019/6/16 19:04
 **/
public class NioClient {

    public static void main(String[] args) {

        Thread thread = new Thread(new SocketChannelHandler(new InetSocketAddress("127.0.0.1", 8088), "您好！"));

        thread.start();

    }

}
