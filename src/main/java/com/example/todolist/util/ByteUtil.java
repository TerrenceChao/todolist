package com.example.todolist.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Component
public class ByteUtil {
    private static ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);

    public byte[] longToBytes(long x) {
        buffer.putLong(0, x);
        return buffer.array();
    }

    public long bytesToLong(byte[] bytes) {
        buffer.put(bytes, 0, bytes.length);
        buffer.flip(); // need flip
        return buffer.getLong();
    }
}
