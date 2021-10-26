package com.example.todolist.db.rmdb.sharding.algorithm;

import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;

import java.util.Collection;

public class LongNumPreciseAlgorithm implements PreciseShardingAlgorithm<Long> {
    @Override
    public String doSharding(Collection<String> collection, PreciseShardingValue<Long> preciseShardingValue) {
        String shardPrefix = getShardPrefix(collection);
        Long value = preciseShardingValue.getValue();
        int size = collection.size();

        return shardPrefix + (value % size + 1);
    }

    private String getShardPrefix(Collection<String> collection) {
        for (String shard : collection) {
            return shard.substring(0, shard.length() - 1);
        }

        throw new IllegalArgumentException("Long number precise algorithm went wrong! Shards NOT FOUND!");
    }
}
