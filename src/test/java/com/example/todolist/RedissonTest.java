package com.example.todolist;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@Slf4j
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class RedissonTest {

    @Autowired
    private RedissonClient redissonClient;

    @Test
    public void test1() throws Exception {
        log.info("redisson setting info: {}", redissonClient.getConfig().toYAML());
    }
}
