package com.zsw.demo.bio;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @author ZhangShaowei on 2019/6/13 10:37
 **/
public class BioClient {


    public void start(String address, int port) throws IOException {

        Socket socket = new Socket(address, port);

        OutputStream outputStream = socket.getOutputStream();

        byte[] data = "你好！".getBytes();
        outputStream.write(data);
        outputStream.flush();
        outputStream.close();

        socket.close();
    }


    public static void main(String[] args) throws Exception {
        new BioClient().start("127.0.0.1", 8088);
    }


}
