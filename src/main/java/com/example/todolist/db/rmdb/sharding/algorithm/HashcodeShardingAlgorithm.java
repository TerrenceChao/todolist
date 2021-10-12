package com.example.todolist.db.rmdb.sharding.algorithm;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HashcodeShardingAlgorithm extends BaseHashcodeAlgorithm {

    @Override
    protected int shardByHashcode(String hashcode) {
        int value = 0;
        for (int i = 0, len = hashcode.length(); i < len; i += 8) {
            value += hashcode.charAt(i);
        }

        return value;
    }
}
