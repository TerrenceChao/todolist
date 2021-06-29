package com.example.todolist.db.rmdb.repo;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.todolist.db.rmdb.entity.TodoTask;
import com.example.todolist.db.rmdb.mapper.TodoTaskMapper;
import com.example.todolist.model.vo.TodoTaskVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.UUID;


@Repository
public class TodoTaskRepository {

    @Autowired
    private TodoTaskMapper todoTaskMapper;

    public Long insert(String title, String content, String attachments, Integer weekOfYear, String createdAt) {
        TodoTask task = new TodoTask()
                .setTitle(title)
                .setContent(content)
                .setAttachments(attachments)
                .setWeekOfYear(weekOfYear)
                .setCreatedAt(createdAt);
        todoTaskMapper.insert(task);

        return task.getTid();
    }

    public TodoTaskVo findOne(Long tid, Integer partitionKey) {
        QueryWrapper<TodoTask> wrapper = new QueryWrapper<TodoTask>()
            .eq("tid", tid)
            .eq("week_of_year", partitionKey); // partition_key

        TodoTask task = todoTaskMapper.selectOne(wrapper);
        return new TodoTaskVo(task);
    }

//    /**
//     * TODO 暫時測試功能用，不會用這種粗糙方式做
//     * @param startTime
//     * @param tid
//     * @param limit
//     * @return
//     */
//    public List<TodoTaskVo> getList(ZonedDateTime startTime, Long tid, Integer limit) {
//        QueryWrapper<TodoTask> wrapper = new QueryWrapper<>();
//        wrapper.ge("created_at", startTime.toString().replace("T", " ").replace("Z", " "));
//        wrapper.ge("tid", tid);
//        wrapper.last(" limit " + limit);
//
//        return todoTaskMapper.selectList(wrapper).stream()
//                .map(task -> new TodoTaskVo(task))
//                .collect(Collectors.toList());
//    }
//
//    /**
//     * TODO 暫時測試功能用，不會用這種粗糙方式做
//     * @param startTime
//     * @param limit
//     * @return
//     */
//    public List<TodoTaskVo> getList(ZonedDateTime startTime, Integer limit) {
//        QueryWrapper<TodoTask> wrapper = new QueryWrapper<>();
//        wrapper.ge("created_at", startTime.toString().replace("T", " ").replace("Z", " "));
//        wrapper.last(" limit " + limit);
//
//        return todoTaskMapper.selectList(wrapper).stream()
//                .map(task -> new TodoTaskVo(task))
//                .collect(Collectors.toList());
//    }
}
