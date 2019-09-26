package com.zsw.demo.nio;

import com.zsw.demo.nio.buffer.Buffers;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author ZhangShaowei on 2019/9/25 14:20
 **/
@Slf4j
public class NioClient {

    private static BlockingQueue<String> queue = new ArrayBlockingQueue<>(100);

    private static final Charset utf8 = Charset.forName("utf-8");

    @SneakyThrows
    public static void main(String[] args) {
        Thread input = new Thread(NioClient::input);
        Thread start = new Thread(NioClient::start);
        input.start();
        start.start();

        TimeUnit.SECONDS.sleep(120);

        input.interrupt();
        start.interrupt();

        System.exit(0);
    }


    private static void input() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                String line = reader.readLine();
                if (line != null && line.length() > 0) {
                    queue.put(line);
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void start() {
        Selector selector;

        try {
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            selector = Selector.open();

            socketChannel.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ, new Buffers(512, 512));
            socketChannel.connect(new InetSocketAddress("127.0.0.1", 8080));

            while (!socketChannel.finishConnect()) {
                log.info("握手 ...");
            }

            log.info("已连接到服务器");
        } catch (IOException e) {
            log.error("客户端连接失败: {}", e.getMessage());
            return;
        }


        try {
            while (!Thread.currentThread().isInterrupted()) {
                selector.select();
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    Buffers buffers = (Buffers) key.attachment();
                    ByteBuffer readBuffer = buffers.getReadBuffer();
                    ByteBuffer writeBuffer = buffers.getWriteBuffer();

                    SocketChannel sc = (SocketChannel) key.channel();

                    if (key.isReadable()) {
                        sc.read(readBuffer);
                        readBuffer.flip();

                        CharBuffer charBuffer = utf8.decode(readBuffer);
                        log.info(new String(charBuffer.array()));

                        readBuffer.clear();
                    }

                    if (key.isWritable()) {
                        String take = queue.poll();
                        if (take == null || take.length() == 0) {
                            take = "Heartbeat ...";
                        }
                        writeBuffer.put(take.getBytes(utf8));
                        writeBuffer.flip();

                        while (writeBuffer.hasRemaining()) {
                            sc.write(writeBuffer);
                        }
                        writeBuffer.clear();
                    }
                }
                TimeUnit.SECONDS.sleep(1);
            }
        } catch (IOException e) {
            log.error("连接错误: {}", e.getMessage());
        } catch (InterruptedException e) {
            log.error("客户端已被终止: {}", e.getMessage());
        } finally {
            try {
                selector.close();
            } catch (IOException e) {
                log.error("selctor 关闭出错");
            }
        }

        log.info("客户端已停止");


    }


}
