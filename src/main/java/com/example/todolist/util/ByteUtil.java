package com.example.todolist.util;

import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Component
public class ByteUtil {

    public byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(0, x);
        return buffer.array();
    }

    public long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes, 0, bytes.length);
        buffer.flip(); // need flip
        return buffer.getLong();
    }
}
