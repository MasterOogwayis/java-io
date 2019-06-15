package com.zsw.demo.nio;

import lombok.Getter;

import java.nio.ByteBuffer;

/**
 * @author Administrator on 2019/6/15 19:27
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
