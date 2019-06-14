package com.zsw.demo.buffer;

import java.nio.ByteBuffer;

/**
 * @author ZhangShaowei on 2019/6/13 15:06
 **/
public class ReadOnlyBuffer {

    public static void main(String[] args) {

        ByteBuffer byteBuffer = ByteBuffer.allocate(10);

        for (int i = 0; i < byteBuffer.capacity(); i++) {
            byteBuffer.put((byte) i);
        }

        ByteBuffer readOnlyBuffer = byteBuffer.asReadOnlyBuffer();

        for (int i = 0; i < byteBuffer.capacity(); i++) {
            byte b = byteBuffer.get(i);
            byteBuffer.put(i, (byte) (b * 10));
        }

        byteBuffer.position(0);
        byteBuffer.limit(byteBuffer.capacity());

        readOnlyBuffer.flip();

        while (readOnlyBuffer.hasRemaining()) {
            System.out.println(readOnlyBuffer.get());
        }

        readOnlyBuffer.put((byte) 1);


    }

}
