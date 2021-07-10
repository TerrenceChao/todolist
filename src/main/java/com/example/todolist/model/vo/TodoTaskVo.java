package com.example.todolist.model.vo;

import com.alibaba.fastjson.JSONObject;
import com.example.todolist.db.rmdb.entity.TodoTask;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class TodoTaskVo extends BaseVo {

    private Long tid;

    private String title;

    private String content;

    /**
     * JSON
     * {
     *      tid: xxxx,
     *      files: [
     *          { name, hash, url },
     *          { name, hash, url },
     *          ...
     *      ]
     * }
     */
    private JSONObject attachments;

    /** partition key */
    private Integer weekOfYear;

    private Date createdAt;

    /** soft delete */
    private Date deletedAt;

    private Date done;

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

    public TodoTaskVo(Long tid, TodoTask task) {
        setTid(tid);
        setTitle(task.getTitle());
        setContent(task.getContent());
        setAttachments(task.getAttachments());
        setWeekOfYear(task.getWeekOfYear());
        setCreatedAt(task.getCreatedAt());
        setDeletedAt(task.getDeletedAt());
        setDone(task.getDone());
    }

    public void setAttachments(String attachmentsStr) {
        this.attachments = JSONObject.parseObject(attachmentsStr);
    }

    public JSONObject toNext() {
        JSONObject json = new JSONObject();
        json.put("seq", tid);
        json.put("createdAt", createdAt);

        return json;
    }
}
