package com.zsw.demo.nio;

/**
 * @author Administrator on 2019/6/15 19:56
 **/
public class NioClient {

    public static void main(String[] args) {
        Thread thread = new Thread(new SocketChannelHandler("127.0.0.1", 8088, "Hello World!"));
        thread.start();
    }

}
