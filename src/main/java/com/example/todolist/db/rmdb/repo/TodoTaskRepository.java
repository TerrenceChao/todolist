package com.example.todolist.db.rmdb.repo;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.todolist.common.Constant;
import com.example.todolist.db.rmdb.entity.TodoTask;
import com.example.todolist.db.rmdb.mapper.TodoTaskMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


@Repository
public class TodoTaskRepository {

    private final String DATETIME_FORMAT = Constant.DATETIME_FORMAT;

    @Autowired
    private TodoTaskMapper todoTaskMapper;

    public TodoTask insert(String title, String content, String attachments, Integer weekOfYear, Date createdAt) {
        TodoTask task = new TodoTask()
            .setTitle(title)
            .setContent(content)
            .setAttachments(attachments)
            .setWeekOfYear(weekOfYear)
            .setCreatedAt(createdAt);
        todoTaskMapper.insert(task);

        return task;
    }

    public TodoTask findByTid(Long tid) {
        QueryWrapper<TodoTask> wrapper = new QueryWrapper<TodoTask>()
                .eq("tid", tid);

        return todoTaskMapper.selectOne(wrapper);
    }

    public TodoTask findOne(Long tid, Integer partitionKey) {
        QueryWrapper<TodoTask> wrapper = new QueryWrapper<TodoTask>()
            .eq("tid", tid)
            .eq("week_of_year", partitionKey); // partition_key

        return todoTaskMapper.selectOne(wrapper);
    }

    /**
     * for hot task
     * @param startTime
     * @param tid
     * @param limit
     * @return
     */
    public List<TodoTask> getList(Date startTime, Long tid, Integer limit) {
        QueryWrapper<TodoTask> wrapper = new QueryWrapper<TodoTask>()
            .ge("created_at", new SimpleDateFormat(DATETIME_FORMAT).format(startTime))
            .ge("tid", tid)
            .orderByAsc("tid")
            .last(" limit " + limit);

        return todoTaskMapper.selectList(wrapper);
    }

    /**
     * for hot task & transform
     * @param startTime
     * @param limit
     * @return
     */
    public List<TodoTask> getList(Date startTime, Integer limit) {
        QueryWrapper<TodoTask> wrapper = new QueryWrapper<TodoTask>()
            .ge("created_at", new SimpleDateFormat(DATETIME_FORMAT).format(startTime))
            .orderByAsc("tid")
            .last(" limit " + limit);

        return todoTaskMapper.selectList(wrapper);
    }

    /**
     * for transform
     * @param tid
     * @param limit
     * @return
     */
    public List<TodoTask> getList(Long tid, Integer limit) {
        QueryWrapper<TodoTask> wrapper = new QueryWrapper<TodoTask>()
            .ge("tid", tid)
            .orderByAsc("tid")
            .last(" limit " + limit);

        return todoTaskMapper.selectList(wrapper);
    }

    /**
     * for transform
     * @param limit
     * @return
     */
    public List<TodoTask> getList(Integer limit) {
        QueryWrapper<TodoTask> wrapper = new QueryWrapper<TodoTask>()
                .orderByAsc("tid")
                .last(" limit " + limit);

        return todoTaskMapper.selectList(wrapper);
    }
}
