package com.example.todolist.db.rmdb.sharding.algorithm;

import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;

import java.util.Collection;

public class IntegerNumPreciseAlgorithm implements PreciseShardingAlgorithm<Integer> {
    @Override
    public String doSharding(Collection<String> collection, PreciseShardingValue<Integer> preciseShardingValue) {
        String shardPrefix = getShardPrefix(collection);
        int value = preciseShardingValue.getValue();
        int size = collection.size();

        return shardPrefix + (value % size + 1);
    }

    private String getShardPrefix(Collection<String> collection) {
        for (String shard : collection) {
            return shard.substring(0, shard.length() - 1);
        }

        throw new IllegalArgumentException("Integer number precise algorithm went wrong! Shards NOT FOUND!");
    }
}
