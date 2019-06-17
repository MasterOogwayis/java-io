package com.zsw.demo.nio;

import lombok.Getter;

import java.nio.ByteBuffer;

/**
 * @author ZhangShaowei on 2019/6/17 11:50
 **/
@Getter
public class Buffers {

    public Buffers(int readCapacity, int writeCapacity) {
        this.readBuffer = ByteBuffer.allocate(readCapacity);
        this.writeBuffer = ByteBuffer.allocate(writeCapacity);
    }

    private ByteBuffer readBuffer;
    private ByteBuffer writeBuffer;

}
