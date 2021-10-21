package com.example.todolist.service;

import com.example.todolist.service.impl.TriggerServiceImplA;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@Slf4j
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class RedissonTransformTest {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private TriggerServiceImplA triggerService;

    @Before
    public void beforeTest() {
        init();
    }

    @Test
    public void test1() {
        int count = 10023;
        for (long i = 0; i < count; i++) {
            redissonProcessTest(i);
        }
    }

    @Test
    public void test2() {
        int count = 1023;
        for (long i = 0; i < count; i++) {
            triggerService.transformAsync(i);
//            log.info("needTransform: {}", res);
        }
    }

    @After
    public void afterTest() {
        String batch = "batch"; // "trans.batch";
        redissonClient.getMap(batch).clear();
    }

    /**
     * TODO
     *  1. get int 'B' from 'batch_idx' (C = 0 ~ 19)
     *  using distribution lock here:
     *  2. task_count_'B' += 1
     *  3. if task_count_'B' == 1 and batch_srt_tid_'B' == null:
     *          1. set 'tid' to batch_srt_tid_'B'
     *     else if task_count_'B' == 100:
     *          1. rename 'tid' from batch_srt_tid_'B' to batch_srt_tid_'B'_lock
     *          2. B = (B + 1) % 20
     *          3. clear task_count_'B'
     *          4. return true
     *  unlock from here:
     *      -> async push 'tid' to message queue
     *  >>>> queue customer:
     *      if update success:
     *          set the 'timestamp' by 'tid'
     *          rm 'tid' from batch_srt_tid_'B'_lock
     *      else:
     *          retry
     *
     */
    private void redissonProcessTest(Long tid) {
        String transformLock = "transformLock";
        Integer lockInSecs = 5;
        String batch = "batch"; // "trans.batch";
        String batchIdx = "idx"; // "trans.batch.idx";
        String batchSrtTid = "tid"; // "trans.batch.start.tid";
        String batchSrtTidLock = "tidLock"; // "trans.batch.start.tid.lock";
        String batchTaskCount = "cnt"; // "trans.batch.task.count";
        Long batchMax = 20L;
        Long todoListMax = 10L;

        RMap<String, Long> rMap = redissonClient.getMapCache(batch);
        rMap.fastPutIfAbsent(batchIdx, 0L);

//        RLock rLock = redissonClient.getLock(transformLock);
//        try {
//            rLock.lock(lockInSecs, TimeUnit.SECONDS);
//            ...
//
//        } catch (Exception e) {
//            log.error("獲取 redission 分布式鎖失敗", e);
//            throw e;
//        } finally {
//            if (rLock != null) {
//                rLock.unlock();
//            }
//        }


        Long idx = rMap.get(batchIdx);
        String prefix = batch + idx;
        String batchSrtTidKey = prefix + batchSrtTid;
        String batchSrtTidLockKey = prefix + batchSrtTidLock;
        String taskCountKey = prefix + batchTaskCount;

        Long count = rMap.get(taskCountKey);
        rMap.fastPut(taskCountKey, count + 1L);

        if (rMap.get(taskCountKey) == 1L && ! rMap.containsKey(batchSrtTidKey)) {
            rMap.fastPut(batchSrtTidKey, tid);
        } else if (rMap.get(taskCountKey) == todoListMax) {
            Long srtTid = rMap.remove(batchSrtTidKey);
            rMap.fastPutAsync(batchSrtTidLockKey, srtTid);
//            rMap.fastPut(batchSrtTidLockKey, srtTid);
            rMap.fastPut(batchIdx, (idx + 1) % batchMax);
            rMap.fastPut(taskCountKey, 0L);
            log.info("rmap idx: {}, tid: {}, count: {}, tid-lock: {}", rMap.get(batchIdx), rMap.get(batchSrtTidKey), rMap.get(taskCountKey), rMap.get(batchSrtTidLockKey));
        }
        log.info("rmap idx: {}, tid: {}, count: {}, tid-lock: {}", rMap.get(batchIdx), rMap.get(batchSrtTidKey), rMap.get(taskCountKey), rMap.get(batchSrtTidLockKey));

    }

    public void init() {
        String batch = "batch"; // "trans.batch";
        String batchIdx = "idx"; // "trans.batch.idx";
        String batchTaskCount = "cnt"; // "trans.batch.task.count";
        Long batchMax = 20L;

        RMap<String, Long> rMap = redissonClient.getMapCache(batch);
        rMap.fastPutIfAbsent(batchIdx, 0L);

        for (long idx = 0; idx < batchMax; idx++) {
            String prefix = batch + idx;
            String taskCountKey = prefix + batchTaskCount;
            rMap.fastPut(taskCountKey, 0L);
        }
    }
}
