package com.example.todolist.db.rmdb.sharding.algorithm;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;

import java.util.Collection;

@Slf4j
public abstract class BaseHashcodeAlgorithm implements PreciseShardingAlgorithm<String> {

    protected abstract int shardByHashcode(String hashcode);

    /**
     * @param databaseNames 所有分片庫的集合
     * @param shardingValue 為分片屬性，其中 logicTableName 為邏輯表，columnName 分片健（字段），value 為 SQL 中解析出的分片健的值
     * @return
     */
    @Override
    public String doSharding(Collection<String> databaseNames, PreciseShardingValue<String> shardingValue) {

        int dbAmount = databaseNames.size();
        String value = shardByHashcode(shardingValue.getValue()) % dbAmount + 1 + "";
        for (String databaseName : databaseNames) {
            if (databaseName.endsWith(value)) {
                return databaseName;
            }
        }

        throw new IllegalArgumentException("Hashcode algorithm went wrong!");
    }
}
