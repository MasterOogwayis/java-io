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
 * @author Administrator on 2019/6/15 19:45
 **/
@Slf4j
@AllArgsConstructor
public class SocketChannelHandler implements Runnable {

    private final Charset utf8 = Charset.forName("utf-8");

    private String address;

    private int port;

    private String message;


    @Override
    public void run() {
        Selector selector;

        try {
            selector = Selector.open();
            SocketChannel sc = SocketChannel.open();
            sc.configureBlocking(false);
            sc.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ, new Buffers(512, 512));
            sc.connect(new InetSocketAddress(address, port));

            while (!sc.finishConnect()) {
                ;
            }

            log.info("连接服务器成功...");
        } catch (IOException e) {
            log.error("客户端启动失败：{}", e.getMessage());
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
                        writeBuffer.clear();
                    }
                    if (key.isReadable()) {
                        sc.read(readBuffer);
                        readBuffer.flip();

                        CharBuffer charBuffer = utf8.decode(readBuffer);
                        log.info("服务器回复：{}", Arrays.toString(charBuffer.array()));

                        readBuffer.clear();
                    }
                }
                Thread.sleep(1000);
            }
        } catch (IOException e) {
            log.error("客户端 Selector 出错");
        } catch (InterruptedException e) {
            log.error("客户端已被终止：{}", e.getMessage());
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
