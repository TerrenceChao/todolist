package com.example.todolist.model.vo;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.todolist.db.rmdb.entity.TodoList;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@Accessors(chain = true)
public class TodoListVo extends BaseVo {

    /**
     * 10 筆 todo_task 的第一筆 tid
     */
    private Long lid;

    private JSONArray todoTasks;

    private ZonedDateTime firstCreatedAt;

    /**
     * 第一筆 created_at 的月份
     * TODO sharding key 平衡 多庫 讀取流量
     */
    private Integer firstMonth;

    /**
     * 第一筆 created_at 的週
     * TODO partition key 平衡 單庫 讀取流量
     */
    private Integer firstWeekOfYear;

    /**
     * 10 筆 todo_task 的最後一筆 created_at
     */
    private ZonedDateTime lastCreatedAt;

    public TodoListVo(TodoList todoList) {
        setLid(todoList.getLid());
        setTodoTasks(todoList.getTodoTasks());
        setFirstCreatedAt(todoList.getFirstCreatedAt());
        setFirstMonth(todoList.getFirstMonth());
        setFirstWeekOfYear(todoList.getFirstWeekOfYear());
        setLastCreatedAt(todoList.getLastCreatedAt());
    }

    public JSONObject toNext() {
        JSONObject json = new JSONObject();
        // seq: 庫 + 表 + lid
        json.put("seq", firstMonth + "-" + firstWeekOfYear + "-" + lid);
        json.put("createdAt", firstCreatedAt);

        return json;
    }

    public List<TodoTaskVo> toTaskList() {
        for (Object item : JSONArray.)
    }
}
