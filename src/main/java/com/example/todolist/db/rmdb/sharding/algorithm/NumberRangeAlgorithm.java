package com.example.todolist.db.rmdb.sharding.algorithm;

import com.google.common.collect.Range;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingValue;

import java.util.Collection;
import java.util.LinkedHashSet;

@Slf4j
public class NumberRangeAlgorithm implements RangeShardingAlgorithm<Long> {

    @Override
    public Collection<String> doSharding(Collection<String> collection, RangeShardingValue<Long> rangeShardingValue) {
        int size = collection.size();
        Collection<String> result = new LinkedHashSet<>(size);
        Range<Long> shardRange = rangeShardingValue.getValueRange();
        long begin = shardRange.hasLowerBound() ? shardRange.lowerEndpoint() : 0L;
        long end   = shardRange.hasUpperBound() ? shardRange.upperEndpoint() : Long.MAX_VALUE;
        log.info("getValueRange >> \nnhasUpperBound:{} -> \nhasUpperBound:{} -> \nlowerEndpoint:{} -> \nupperEndpoint:{}\n",
                shardRange.hasLowerBound(),
                shardRange.hasUpperBound(),
                begin,
                end
        );

        for (String shard : collection) {
            log.info("shard >> {}", shard);
            result.add(shard);
        }

        return result;
    }
}
