package com.zsw.demo.io.bio;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ZhangShaowei on 2019/9/24 10:13
 **/
@Slf4j
public class BioServer {

    @SneakyThrows
    public static void main(String[] args) {
        startServer(8080);
    }


    public static void startServer(int port) {

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                4,
                20,
                60,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(),
                new ServerThreadFactory()
        );

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                executor.execute(new ServerHandler(serverSocket.accept()));
                log.info("a client has connected to server ...");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    @AllArgsConstructor
    static class ServerHandler implements Runnable {

        private Socket socket;

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
                 PrintWriter writer = new PrintWriter(this.socket.getOutputStream(), true)) {
                while (true) {
                    String line = reader.readLine();
                    if ("".equals(line) || line.length() == 0) {
                        continue;
                    } else if ("bye".equals(line)) {
                        this.socket.close();
                        break;
                    }

                    log.info("receive message: {}", line);

                    writer.println("Echo from server: " + line);
                    writer.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    static class ServerThreadFactory implements ThreadFactory {

        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        ServerThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = "pool-" +
                    poolNumber.getAndIncrement() +
                    "-server-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }


}
