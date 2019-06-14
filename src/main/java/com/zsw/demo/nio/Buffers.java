package com.zsw.demo.nio;

import lombok.Getter;

import java.nio.ByteBuffer;

/**
 * @author ZhangShaowei on 2019/6/14 9:53
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
