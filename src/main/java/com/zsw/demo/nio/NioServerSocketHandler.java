package com.zsw.demo.nio;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Iterator;

/**
 * @author ZhangShaowei on 2019/6/14 9:49
 **/
@Slf4j
@AllArgsConstructor
public class NioServerSocketHandler implements Runnable {

    private int port;

    private final Charset utf8 = Charset.forName("utf-8");

    @Override
    public void run() {
        Selector selector;
        ServerSocketChannel ssc;

        try {
            ssc = ServerSocketChannel.open();
            ssc.configureBlocking(false);
            ssc.bind(new InetSocketAddress(port));

            selector = Selector.open();
            ssc.register(selector, SelectionKey.OP_ACCEPT);

            log.info("服务器已启动...");

        } catch (Exception e) {
            log.error("服务器启动失败", e.getMessage());
            return;
        }


        try {
            while (!Thread.currentThread().isInterrupted()) {
                selector.select();
                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    it.remove();

                    try {
                        if (key.isAcceptable()) {
                            SocketChannel sc = ssc.accept();
                            sc.configureBlocking(false);
                            sc.register(selector, SelectionKey.OP_READ, new Buffers(512, 512));

                            log.info("一个客户端连接了：{}", sc.getRemoteAddress());
                        } else if (key.isReadable()) {
                            SocketChannel sc = (SocketChannel) key.channel();

                            Buffers buffers = (Buffers) key.attachment();
                            ByteBuffer writeBuffer = buffers.getWriteBuffer();
                            ByteBuffer readBuffer = buffers.getReadBuffer();

                            sc.read(readBuffer);
                            readBuffer.flip();

                            CharBuffer decode = utf8.decode(readBuffer);
                            log.info("Echo from client: {}", Arrays.toString(decode.array()));

                            readBuffer.rewind();
                            writeBuffer.put("Echo from server:".getBytes(utf8));
                            writeBuffer.put(readBuffer);

                            readBuffer.clear();

                            key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
                        } else if (key.isWritable()) {
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
                        log.error("客户端连接失败：{}", e.getMessage());
                        key.cancel();
                        key.channel().close();
                    }
                }
                Thread.sleep(500);
            }
        } catch (IOException e) {
            log.error("服务器 Selector 出错：{}", e.getCause());
        } catch (InterruptedException e) {
            log.error("服务已被终止：{}", e.getMessage());
        } finally {
            try {
                selector.close();
            } catch (IOException e) {
                log.error("服务器关闭 Selector 失败：{}", e.getMessage());
            }
            log.info("服务器已关闭 selector");
        }





    }
}
