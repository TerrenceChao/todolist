package com.example.todolist.model.vo;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.todolist.db.rmdb.entity.TodoList;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Data
@Accessors(chain = true)
public class TodoListVo extends BaseVo {

    /**
     * 100 筆 todo_task 的第一筆 tid
     */
    private Long lid;

    private JSONArray todoTasks;

    private Date firstCreatedAt;

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
     * 100 筆 todo_task 的最後一筆 created_at
     */
    private Date lastCreatedAt;

    public TodoListVo(TodoList todoList) {
        setLid(todoList.getLid());
        setTodoTasks(todoList.getTodoTasks());
        setFirstCreatedAt(todoList.getFirstCreatedAt());
        setFirstMonth(todoList.getFirstMonth());
        setFirstWeekOfYear(todoList.getFirstWeekOfYear());
        setLastCreatedAt(todoList.getLastCreatedAt());
    }

    public void setTodoTasks(String todoTasksStr) {
        this.todoTasks = JSONArray.parseArray(todoTasksStr);
    }

    public JSONObject toNext() {
        JSONObject json = new JSONObject();
        // seq: 庫 + 表 + lid
        json.put("seq", firstMonth + "-" + firstWeekOfYear + "-" + lid);
        json.put("createdAt", firstCreatedAt);

        return json;
    }

    /**
     *
     * @param seq
     * @return
     */
    public static TodoSeqVo parseSeq(String seq) {
        log.info("parse seq: {}", seq);
        String[] values = seq.split("-");

        TodoSeqVo seqVo = new TodoSeqVo()
            .setMonth(Integer.valueOf(values[0]))
            .setWeekOfYear(Integer.valueOf(values[1]))
            .setLid(Long.valueOf(values[2]));

        log.info("parse content: {}", seqVo);

        return seqVo;
    }

    /**
     * merged tasks from todo_list
     * @return
     */
    public List<TodoTaskVo> toTaskVos() {
        List<TodoTaskVo> taskVos = new ArrayList<>();
        for (int i = todoTasks.size() - 1; i >= 0; i--) {
            JSONObject taskJson = todoTasks.getJSONObject(i);
            TodoTaskVo taskVo = toTaskVo(taskJson);
            taskVos.add(0, taskVo);
        }

        return taskVos;
    }

    /**
     * parse json from todo_list
     * @param taskJson
     * @return
     */
    private TodoTaskVo toTaskVo(JSONObject taskJson) {
        return new TodoTaskVo()
                .setTid(taskJson.getLong("tid"))
                .setTitle(taskJson.getString("title"))
                .setContent(taskJson.getString("content"))
                .setWeekOfYear(taskJson.getInteger("weekOfYear"))
                .setDeletedAt(taskJson.getDate("deletedAt"));
    }

}
