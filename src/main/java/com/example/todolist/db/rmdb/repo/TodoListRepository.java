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

    public List<TodoList> getBatchTodoList(Date startTime, Integer month, Integer weekOfYear, Integer limit) {
        QueryWrapper<TodoList> wrapper = new QueryWrapper<TodoList>()
                .ge("created_at", new SimpleDateFormat(DATETIME_FORMAT).format(startTime)) // >=
                .eq("first_month", month) // equal
                .eq("first_week_of_year", weekOfYear) // equal
                .orderByAsc("lid")
                .last(" limit " + limit);

        return todoListMapper.selectList(wrapper);
    }

    public List<TodoList> getBatchTodoList(Date startTime, Integer month, Integer weekOfYear, Long lid, Integer limit) {
        QueryWrapper<TodoList> wrapper = new QueryWrapper<TodoList>()
                .ge("created_at", new SimpleDateFormat(DATETIME_FORMAT).format(startTime)) // >=
                .eq("first_month", month) // equal
                .eq("first_week_of_year", weekOfYear) // equal
                .ge("lid", lid) // >=
                .orderByAsc("lid")
                .last(" limit " + limit);

        return todoListMapper.selectList(wrapper);
    }
}
