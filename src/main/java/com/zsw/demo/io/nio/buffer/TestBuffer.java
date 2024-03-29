package com.zsw.demo.io.nio.buffer;

import java.nio.IntBuffer;

/**
 * @author ZhangShaowei on 2019/9/25 13:20
 **/
public class TestBuffer {

    public static void main(String[] args) {
        IntBuffer intBuffer = IntBuffer.allocate(8);

        for (int i = 0; i < intBuffer.capacity(); i++) {
            intBuffer.put(2 * (i + 1));
        }

        System.err.println(intBuffer);

//        intBuffer.position(0);


        while (intBuffer.hasRemaining()) {
            System.out.println(intBuffer.get());
        }

        intBuffer.put(1);
        System.out.println(intBuffer);

        intBuffer.clear();
    }

}
