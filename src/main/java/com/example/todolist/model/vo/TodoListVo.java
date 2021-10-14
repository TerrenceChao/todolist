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
     * K 筆 todo_task 的第一筆 tid
     */
    private Long lid;

    private Date firstCreatedAt;

    /**
     * 第 1 筆 created_at 的月份
     * TODO sharding key 平衡 多庫 讀取流量
     */
    private Integer firstMonth;

    /**
     * 第 1 筆 created_at 的週
     * TODO partition key 平衡 單庫 讀取流量
     */
    private Integer firstWeekOfYear;

    private JSONArray todoTasks;

//    /**
//     * K 筆 todo_task 的最後一筆 lid
//     */
//    private Long lastLid;

    /**
     * "下一批" K 筆 todo_task 的第一筆 tid
     */
    private Long nextLid;

    /**
     *  "下一批" K 筆 todo_task 的第一筆 created_at
     */
    private Date nextCreatedAt;

    public TodoListVo(TodoList todoList) {
        setLid(todoList.getLid());

        setFirstCreatedAt(todoList.getFirstCreatedAt());
        setFirstMonth(todoList.getFirstMonth());
        setFirstWeekOfYear(todoList.getFirstWeekOfYear());

        setTodoTasks(todoList.getTodoTasks());
//        setLastLid(todoList.getLastLid());

        setNextLid(todoList.getNextLid());
        setNextCreatedAt(todoList.getNextCreatedAt());
    }

    public void setTodoTasks(String todoTasksStr) {
        this.todoTasks = JSONArray.parseArray(todoTasksStr);
    }

    public JSONObject toNext() {
        JSONObject json = new JSONObject();
        // seq: lid
        json.put("seq", nextLid);
        json.put("createdAt", nextCreatedAt);

        return json;
    }

    /**
     * TODO Deprecated
     * @param seq
     * @return
     */
    public static TodoSeqVo parseSeq(String seq) {
        log.info("parse seq: {}", seq);
        String[] values = seq.split("-");

        TodoSeqVo seqVo = new TodoSeqVo()
            .setMonth(Integer.parseInt(values[0]))
            .setWeekOfYear(Integer.parseInt(values[1]))
            .setLid(Long.parseLong(values[2]));

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
            .setCreatedAt(taskJson.getDate("createdAt"))
            .setDeletedAt(taskJson.getDate("deletedAt"));
    }

}
