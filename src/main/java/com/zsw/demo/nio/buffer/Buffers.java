package com.zsw.demo.nio.buffer;

import lombok.Getter;

import java.nio.ByteBuffer;

/**
 * @author ZhangShaowei on 2019/9/25 13:38
 **/
@Getter
public class Buffers {

    private ByteBuffer readBuffer;
    private ByteBuffer writeBuffer;

    public Buffers(int readCapacity, int writeCapacity) {
        this.readBuffer = ByteBuffer.allocate(readCapacity);
        this.writeBuffer = ByteBuffer.allocate(writeCapacity);
    }
}
