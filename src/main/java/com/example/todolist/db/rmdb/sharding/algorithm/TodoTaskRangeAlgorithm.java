package com.example.todolist.db.rmdb.sharding.algorithm;

import org.apache.shardingsphere.api.sharding.standard.RangeShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingValue;

import java.util.Collection;
import java.util.LinkedHashSet;

public class TodoTaskRangeAlgorithm implements RangeShardingAlgorithm<Long> {

    /**
     * 業務邏輯中採用的 SQL 有 ">= tid" 的需求，因此需要判斷從哪些 shard 取得；
     * 由於 tid 會被安排在哪些 shard 不能確定，因此需要回傳全數的 shard。
     * @param collection
     * @param rangeShardingValue
     * @return
     */
    @Override
    public Collection<String> doSharding(Collection<String> collection, RangeShardingValue<Long> rangeShardingValue) {
        Collection<String> result = new LinkedHashSet<>(collection);
        return result;
    }
}
