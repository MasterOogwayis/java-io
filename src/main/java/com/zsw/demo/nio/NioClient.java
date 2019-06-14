package com.zsw.demo.nio;

/**
 * @author ZhangShaowei on 2019/6/14 13:28
 **/
public class NioClient {

    public static void main(String[] args) {
        Thread t = new Thread(new NioClientSocketHandler("127.0.0.1", 8088, "您好！"));

        t.start();



    }


}
