package com.example.todolist;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.todolist.common.Constant;
import com.example.todolist.db.rmdb.entity.TodoTask;
import com.example.todolist.db.rmdb.mapper.TodoTaskMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
class TodolistApplicationTests {

    @Autowired
    private TodoTaskMapper todoTaskMapper;

    /**
     * test sharding
     * 平均寫入 分散寫入流量
     */
    @Test
    void addTodoTaskDb() {
        int cnt = 20;
        long start = System.currentTimeMillis();
        while (cnt-- > 0) {
            Long t = System.currentTimeMillis() % 1000;
            TodoTask task = new TodoTask()
                    .setTitle("todo " + cnt)
                    .setContent(UUID.randomUUID().toString())
                    .setWeekOfYear(t.intValue())
                    .setCreatedAt(null);
            todoTaskMapper.insert(task);

            // TODO 透過這方式來找到 tid !!
            log.info("來看看 SNOWFLAKE, tid: {}", task.getTid());
        }
        long cost = System.currentTimeMillis() - start;
        System.out.println(cost + " ms");
    }

    /**
     * 多庫多表查詢 造成壓力大
     */
    @Test
    void findTodoTasksDb() {
        QueryWrapper<TodoTask> wrapper = new QueryWrapper<>();
//        wrapper.ge("created_at", new SimpleDateFormat(Constant.DATETIME_FORMAT).format(new Date()));
        wrapper.ge("created_at", "2021-06-28 14:13:52");
        wrapper.orderByAsc("tid");
        wrapper.last(" limit 3");
        List<TodoTask> tasks = todoTaskMapper.selectList(wrapper);
        log.info("清單列表吧! {}", tasks.toString());
    }

    /**
     * 單一筆資料。單庫單表查詢 速度快
     */
    @Test
    void findOneTodoTaskDb() {
        QueryWrapper<TodoTask> wrapper = new QueryWrapper<>();
        wrapper.eq("tid", 1409394602225553410L);
        wrapper.eq("week_of_year", 113); // partition_key
        TodoTask task = todoTaskMapper.selectOne(wrapper);
        log.info("就一筆清單! {}", task);
    }
}
