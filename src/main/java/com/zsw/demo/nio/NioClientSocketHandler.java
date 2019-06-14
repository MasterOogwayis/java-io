package com.zsw.demo.nio;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Iterator;

/**
 * @author ZhangShaowei on 2019/6/14 13:19
 **/
@Slf4j
@AllArgsConstructor
public class NioClientSocketHandler implements Runnable {

    private final Charset utf8 = Charset.forName("utf-8");

    private String address;

    private int port;

    private String message;


    @Override
    public void run() {
        Selector selector;
        try {
            SocketChannel sc = SocketChannel.open();
            sc.configureBlocking(false);
            selector = Selector.open();

            sc.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, new Buffers(512, 512));

            sc.connect(new InetSocketAddress(address, port));

            while (!sc.finishConnect()) {
                ;
            }

            log.info("已连接到服务端");
        } catch (IOException e) {
            log.error("服务器连接失败：{}", e.getMessage());
            return;
        }


        try {
            int i = 0;
            while (!Thread.currentThread().isInterrupted()) {
                selector.select();
                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next();

                    SocketChannel sc = (SocketChannel) key.channel();

                    Buffers buffers = (Buffers) key.attachment();
                    ByteBuffer readBuffer = buffers.getReadBuffer();
                    ByteBuffer writeBuffer = buffers.getWriteBuffer();

                    if (key.isReadable()) {
                        sc.read(readBuffer);

                        readBuffer.flip();

                        CharBuffer charBuffer = utf8.decode(readBuffer);
                        log.info(Arrays.toString(charBuffer.array()));
                        readBuffer.clear();

                    } else if (key.isWritable()) {
                        writeBuffer.put((message + " " + i++).getBytes(utf8));
                        writeBuffer.flip();

                        while (writeBuffer.hasRemaining()) {
                            sc.write(writeBuffer);
                        }
                        writeBuffer.clear();
                    }
                }
                Thread.sleep(1000);

            }
        } catch (IOException e) {
            log.error("连接错误：{}", e.getMessage());
        } catch (InterruptedException e) {
            log.error("客户端已被终止：{}", e.getMessage());
        } finally {
            try {
                selector.close();
            } catch (IOException e) {
                log.error("关闭 Selector 失败：{}", e.getMessage());
            }
            log.info("客户端已关闭");
        }


    }


}
