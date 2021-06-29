package com.example.todolist.model.vo;

import com.alibaba.fastjson.JSONArray;
import com.example.todolist.db.rmdb.entity.TodoTask;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TodoTaskVo extends BaseVo {

    private Long tid;

    private String title;

    private String content;

    /**
     * JSON
     * [
     *      { name, hash, url },
     *      { name, hash, url },
     *      ...
     * ]
     */
    private JSONArray attachments;

    /** partition key */
    private Integer weekOfYear;

    private String createdAt;

    /** soft delete */
    private String deletedAt;

    private String done;

    public TodoTaskVo(TodoTask task) {
        setTid(task.getTid());
        setTitle(task.getTitle());
        setContent(task.getContent());
        setAttachments(task.getAttachments());
        setWeekOfYear(task.getWeekOfYear());
        setCreatedAt(task.getCreatedAt());
        setDeletedAt(task.getDeletedAt());
        setDone(task.getDone());
    }

//    public JSONObject toNext() {
//        JSONObject json = new JSONObject();
//        json.put("createdAt", createdAt);
//        json.put("tid", tid);
//
//        return json;
//    }
}
