package com.zsw.demo.io.nio;

import com.zsw.demo.io.nio.buffer.Buffers;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * @author ZhangShaowei on 2019/9/25 13:30
 **/
@Slf4j
public class NioServer {

    public static final Charset utf8 = Charset.forName("utf-8");

    @SneakyThrows
    public static void main(String[] args) {
        Thread thread = new Thread(NioServer::start);
        thread.start();
        TimeUnit.SECONDS.sleep(300);
        thread.interrupt();
    }

    private static void start() {
        ServerSocketChannel serverSocketChannel = null;
        Selector selector;
        SocketAddress socketAddress = new InetSocketAddress("127.0.0.1", 8080);

        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(socketAddress);

            selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            log.info("Server started at {}", socketAddress);

        } catch (IOException e) {
            log.error("服务器启动失败");
            e.printStackTrace();
            return;
        }

        try {
            while (!Thread.currentThread().isInterrupted()) {
                // 这是个阻塞方法，当且仅当收到事件才会返回
                selector.select();
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();

                SelectionKey key;
                while (iterator.hasNext()) {
                    key = iterator.next();
                    iterator.remove();

                    try {
                        // 注意 channel 是双全工，所以不能用if else
                        // 同一时刻可能有多个事件抵达
                        if (key.isAcceptable()) {
                            // 连接事件
                            ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                            SocketChannel accept = ssc.accept();
                            accept.configureBlocking(false);
                            accept.register(selector, SelectionKey.OP_READ, new Buffers(512, 512));
                            log.info("A client has connected to server, {}", accept.getRemoteAddress());
                        }

                        if (key.isReadable()) {
                            // 读事件
                            SocketChannel sc = (SocketChannel) key.channel();
                            // 附件
                            Buffers buffers = (Buffers) key.attachment();
                            ByteBuffer readByffer = buffers.getReadBuffer();
                            ByteBuffer writeBuffer = buffers.getWriteBuffer();

                            sc.read(readByffer);
                            readByffer.flip();

                            CharBuffer charBuffer = utf8.decode(readByffer);
                            log.info("Receive message from client: {}", new String(charBuffer.array()));

                            readByffer.rewind();
                            writeBuffer.put("Echo from server:".getBytes(utf8));
                            writeBuffer.put(readByffer);

                            readByffer.clear();

                            // 设置写事件
                            key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
                        }

                        if (key.isWritable()) {
                            // 写事件
                            SocketChannel sc = (SocketChannel) key.channel();
                            Buffers buffers = (Buffers) key.attachment();
                            ByteBuffer writeBuffer = buffers.getWriteBuffer();
                            writeBuffer.flip();

                            int len = 0;
                            while (writeBuffer.hasRemaining()) {
                                len = sc.write(writeBuffer);
                                if (len == 0) {
                                    break;
                                }
                            }
                            writeBuffer.compact();
                            if (len != 0) {
                                key.interestOps(key.interestOps() & (~SelectionKey.OP_WRITE));
                            }


                        }

                    } catch (IOException e) {
                        log.error("客户端连接已中断: {}", e.getMessage());
                        key.cancel();
                        key.channel().close();
                    } catch (Exception e) {
                        log.error("异常", e);
                    }
                }
                TimeUnit.MILLISECONDS.sleep(ThreadLocalRandom.current().nextInt(500));
            }
        } catch (IOException e) {
            log.error("Server selector error: {}", e.getMessage());
        } catch (InterruptedException e) {
            log.error("Server has bean interrupted ...{}", e.getMessage());
        } finally {
            try {
                selector.close();
            } catch (IOException e) {
                log.error("Close selector faild: {}", e.getMessage());
            }
        }
        log.info("服务器已关闭 ...");
    }


}
