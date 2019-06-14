package com.zsw.demo.bio;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;

/**
 * @author ZhangShaowei on 2019/6/13 10:33
 **/
public class BioServer {



    public void start(int port) throws IOException {
        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(port);

            while (true) {
                Socket accept = serverSocket.accept();
                InputStream inputStream = accept.getInputStream();

                byte[] buff = new byte[1024];

                int len;
                while ((len = inputStream.read(buff)) > 0) {
                    String msg = new String(buff, 0, len);
                    System.out.println("收到客户端消息: " + msg);
                }

            }

        } finally {
            if (Objects.nonNull(serverSocket)) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }


    public static void main(String[] args) throws Exception {
        new BioServer().start(8088);
    }


}
