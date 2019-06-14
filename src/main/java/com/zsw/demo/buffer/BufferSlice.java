package com.zsw.demo.buffer;

import java.nio.ByteBuffer;

/**
 * @author ZhangShaowei on 2019/6/13 14:58
 **/
public class BufferSlice {

    public static void main(String[] args) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(10);

        for (int i = 0; i < byteBuffer.capacity(); i++) {
            byteBuffer.put((byte) i);
        }

        byteBuffer.position(3);
        byteBuffer.limit(7);
        ByteBuffer slice = byteBuffer.slice();

        for (int i = 0; i < slice.capacity(); i++) {
            byte b = slice.get(i);
            slice.put(i, (byte) (b * 10));
        }

        byteBuffer.position(0);
        byteBuffer.limit(byteBuffer.capacity());

        while (byteBuffer.hasRemaining()) {
            System.out.println(byteBuffer.get());
        }

    }


}
