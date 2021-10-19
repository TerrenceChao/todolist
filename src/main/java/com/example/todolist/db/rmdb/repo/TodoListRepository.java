package com.example.todolist.db.rmdb.repo;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.todolist.common.Constant;
import com.example.todolist.db.rmdb.entity.TodoList;
import com.example.todolist.db.rmdb.mapper.TodoListMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Repository
public class TodoListRepository {

    private final String DATETIME_FORMAT = Constant.DATETIME_FORMAT;

    @Autowired
    private TodoListMapper todoListMapper;

    public TodoList insert(TodoList todoList) {
        todoListMapper.insert(todoList);
        return todoList;
    }

    public List<TodoList> getBatchTodoList(Date startTime, Integer month, Integer weekOfYear, Integer limit) {
        QueryWrapper<TodoList> wrapper = new QueryWrapper<TodoList>()
                .ge("first_created_at", new SimpleDateFormat(DATETIME_FORMAT).format(startTime))
                .eq("first_month", month)
                .eq("first_week_of_year", weekOfYear)
                .orderByAsc("lid")
                .last(" limit " + limit);

        return todoListMapper.selectList(wrapper);
    }

    /**
     * Key point: first_created_at <= [target] && next_lid <= [target]
     * @param startTime
     * @param month
     * @param weekOfYear
     * @param lid
     * @param limit
     * @return
     */
    public List<TodoList> getBatchTodoList(Date startTime, Integer month, Integer weekOfYear, Long lid, Integer limit) {
        QueryWrapper<TodoList> wrapper = new QueryWrapper<TodoList>()
                .ge("first_created_at", new SimpleDateFormat(DATETIME_FORMAT).format(startTime))
                .eq("first_month", month)
                .eq("first_week_of_year", weekOfYear)
                // TODO add index or be the PK for 'next_lid'
                .ge("next_lid", lid)
                .orderByAsc("lid")
                .last(" limit " + limit);

        return todoListMapper.selectList(wrapper);
    }

    public TodoList getLatestOne() {
        QueryWrapper<TodoList> wrapper = new QueryWrapper<TodoList>()
                .select("next_lid")
                // 用有 index 的欄位排序
                .orderByDesc("lid")
                .last(" limit " + 1);

        return todoListMapper.selectOne(wrapper);
    }

    public TodoList getLatestOne(Date startTime, Integer month, Integer weekOfYear) {
        QueryWrapper<TodoList> wrapper = new QueryWrapper<TodoList>()
                .select("next_lid", "next_created_at")
                .ge("first_created_at", new SimpleDateFormat(DATETIME_FORMAT).format(startTime))
                .eq("first_month", month)
                .eq("first_week_of_year", weekOfYear)
                // 用有 index 的欄位-倒排序
                .orderByDesc("lid")
                .last(" limit " + 1);

        return todoListMapper.selectOne(wrapper);
    }
}
