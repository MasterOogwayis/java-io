package com.zsw.demo.buffer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.netty.util.CharsetUtil.UTF_8;

/**
 * @author ZhangShaowei on 2019/10/10 13:58
 **/
class StaticTests {



    public static void main(String[] args) {
//        ByteBuffer byteBuffer = ByteBuffer.allocate(88);
//        String message = "这是一个 netty demo！";
//        byteBuffer.put(message.getBytes(UTF_8));
//        byteBuffer.flip();
//        byte[] data = new byte[byteBuffer.remaining()];
//        byteBuffer.get(data);
//
//        System.out.println(new String(data, UTF_8));
//
//        ByteBuf byteBuf = Unpooled.copiedBuffer("1", UTF_8);
//        byte b = byteBuf.readByte();
//        System.err.println(b);

        int[] SIZE_TABLE;

            List<Integer> sizeTable = new ArrayList<Integer>();
            for (int i = 16; i < 512; i += 16) {
                sizeTable.add(i);
            }

            for (int i = 512; i > 0; i <<= 1) {
                sizeTable.add(i);
            }

            SIZE_TABLE = new int[sizeTable.size()];
            for (int i = 0; i < SIZE_TABLE.length; i ++) {
                SIZE_TABLE[i] = sizeTable.get(i);
            }

        System.out.println(Arrays.toString(SIZE_TABLE));

    }

}
