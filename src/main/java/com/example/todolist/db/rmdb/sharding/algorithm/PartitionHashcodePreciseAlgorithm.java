package com.example.todolist.db.rmdb.sharding.algorithm;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PartitionHashcodePreciseAlgorithm extends BaseHashcodePreciseAlgorithm {

    @Override
    protected int shardByHashcode(String hashcode) {
        return hashcode.charAt(0);
    }
}
