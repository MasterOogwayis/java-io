package com.zsw.demo.nio;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Iterator;

/**
 * @author Administrator on 2019/6/15 19:29
 **/
@Slf4j
@AllArgsConstructor
public class ServerSocketChannelHandler implements Runnable {

    private final Charset utf8 = Charset.forName("utf-8");

    private int port;

    @Override
    public void run() {
        Selector selector;
        ServerSocketChannel ssc;

        try {
            selector = Selector.open();

            ssc = ServerSocketChannel.open();
            ssc.configureBlocking(false);
            ssc.register(selector, SelectionKey.OP_ACCEPT, new Buffers(512, 512));
            ssc.bind(new InetSocketAddress(port));

            log.info("服务器已启动...");
        } catch (IOException e) {
            log.error("服务器启动失败：{}", e.getMessage());
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
                            log.info("一个客户端连接到服务器：{}", sc.getRemoteAddress());
                        }
                        if (key.isReadable()) {
                            SocketChannel sc = (SocketChannel) key.channel();
                            Buffers buffers = (Buffers) key.attachment();
                            ByteBuffer readBuffer = buffers.getReadBuffer();
                            ByteBuffer writeBuffer = buffers.getWriteBuffer();

                            sc.read(readBuffer);
                            readBuffer.flip();

                            CharBuffer charBuffer = utf8.decode(readBuffer);
                            log.info("接收到客户端消息：{}", Arrays.toString(charBuffer.array()));

                            readBuffer.rewind();

                            writeBuffer.put("回复：".getBytes(utf8));
                            writeBuffer.put(readBuffer);
                            readBuffer.clear();

                            key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
                        }
                        if (key.isWritable()) {

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

                            // 不用 clear 是因为此时可能已经又收到数据
                            writeBuffer.compact();

                            if (len != 0) {
                                key.interestOps(key.interestOps() & (~SelectionKey.OP_WRITE));
                            }

                        }
                    } catch (IOException e) {
                        log.error("客户端连接出错：{}", e.getMessage());
                        key.cancel();
                        key.channel().close();
                    }
                }
                Thread.sleep(500);
            }
        } catch (InterruptedException e) {
            log.error("服务器已被终止：{}", e.getMessage());
        } catch (IOException e) {
            log.error("服务器 Selector 出错：{}", e.getMessage());
        } finally {
            try {
                selector.close();
            } catch (IOException e) {
                log.error("关闭 Selector 出错：{}", e.getMessage());
            }
            log.info("服务器已关闭");
        }


    }
}
