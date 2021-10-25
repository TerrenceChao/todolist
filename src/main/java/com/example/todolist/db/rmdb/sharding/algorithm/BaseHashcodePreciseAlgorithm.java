package com.example.todolist.db.rmdb.sharding.algorithm;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;

import java.util.Collection;

@Slf4j
public abstract class BaseHashcodePreciseAlgorithm implements PreciseShardingAlgorithm<String> {

    protected abstract int shardByHashcode(String hashcode);

    /**
     * @param collection databaseNames or tableNames >> 所有分庫 or 分片的集合
     * @param preciseShardingValue 為分片屬性，其中 logicTableName 為邏輯表，columnName 分片健（字段），value 為 SQL 中解析出的分片健的值
     * @return
     */
    @Override
    public String doSharding(Collection<String> collection, PreciseShardingValue<String> preciseShardingValue) {

        int size = collection.size();
        String value = shardByHashcode(preciseShardingValue.getValue()) % size + 1 + "";
        for (String shard : collection) {
            if (shard.endsWith(value)) {
                return shard;
            }
        }

        throw new IllegalArgumentException("Hashcode precise algorithm went wrong!");
    }
}
