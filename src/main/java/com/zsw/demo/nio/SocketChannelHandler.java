package com.zsw.demo.nio;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Iterator;

/**
 * @author ZhangShaowei on 2019/6/17 13:15
 **/
@Slf4j
@AllArgsConstructor
public class SocketChannelHandler implements Runnable {

    private SocketAddress address;

    private String message;

    private final Charset utf8 = Charset.forName("utf-8");


    @Override
    public void run() {
        Selector selector;

        try {
            selector = Selector.open();
            SocketChannel sc = SocketChannel.open();
            sc.configureBlocking(false);
            sc.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, new Buffers(512, 512));
            sc.connect(address);

            while (!sc.finishConnect()) {
                ;
            }

            log.info("服务器连接成功");

        } catch (IOException e) {
            log.error("连接服务器失败：{}", e.getMessage());
            return;
        }

        try {
            while (!Thread.currentThread().isInterrupted()) {
                selector.select();
                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    SocketChannel sc = (SocketChannel) key.channel();
                    Buffers buffers = (Buffers) key.attachment();

                    ByteBuffer readBuffer = buffers.getReadBuffer();
                    ByteBuffer writeBuffer = buffers.getWriteBuffer();

                    if (key.isWritable()) {
                        writeBuffer.put(message.getBytes(utf8));
                        writeBuffer.flip();
                        while (writeBuffer.hasRemaining()) {
                            sc.write(writeBuffer);
                    }

                    if (key.isReadable()) {
                    }
                    writeBuffer.clear();
                        sc.read(readBuffer);
                        readBuffer.flip();

                        CharBuffer charBuffer = utf8.decode(readBuffer);
                        log.info(Arrays.toString(charBuffer.array()));

                        readBuffer.clear();
                    }
                }
                Thread.sleep(1000);
            }
        } catch (IOException e) {
            log.error("客户端 Selector 出错：{}", e.getMessage());
        } catch (InterruptedException e) {
            log.error("客户端已被终止：{}", e.getCause());
        } finally {
            try {
                selector.close();
            } catch (IOException e) {
                log.error("客户端关闭 Selector 出错：{}", e.getMessage());
            }
            log.info("客户端已关闭");
        }


    }
}
