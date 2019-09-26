package com.zsw.demo.bio;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;

/**
 * @author ZhangShaowei on 2019/9/24 10:40
 **/
@Slf4j
public class BioClient {

    public static void main(String[] args) {
        startClient("127.0.0.1", 8080);
    }


    private static void startClient(String address, int port) {
        try (Socket socket = new Socket(address, port);
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            new Thread() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            String echoFromServer = in.readLine();
                            log.info(echoFromServer);
                        } catch (IOException e) {
                            e.printStackTrace();
                            break;
                        }
                    }
                }
            }.start();

            while (true) {
                String line = reader.readLine();
                if ("exit".equalsIgnoreCase(line)) {
                    break;
                }
                out.println(line);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
