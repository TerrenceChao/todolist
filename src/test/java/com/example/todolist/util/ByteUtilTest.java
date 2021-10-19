package com.example.todolist.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@Slf4j
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class ByteUtilTest {

    @Autowired
    private ByteUtil byteUtil;

    @Test
    public void test1() {
        // arrange
        long val = 1448542555033419777L;

        // act
        byte[] res = byteUtil.longToBytes(val);
        long val2 = byteUtil.bytesToLong(res);
        log.info("longToBytes  \nlong: {}, \nbytes: {}, \nback to long: {}", val, res, val2);

        // assert
        Assert.assertEquals(val2, val);
    }
}
