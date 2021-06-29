package com.example.todolist.db.rmdb.repo;

import com.example.todolist.db.rmdb.mapper.TodoListMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class TodoListRepository {

    @Autowired
    private TodoListMapper todoListMapper;

//    public List<TodoLis>
}
