package com.example.todolist.db.rmdb.entity;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 寫入用
 */
@Data
@Accessors(chain = true)
@TableName(value = "todo_task")
public class TodoTask {

    /**
     * TODO 讀/寫 用不同的 sharding key 這件事有點難做; 後來想到用不同的 db schema!
     */

    /** sharding key 平衡 多庫 寫入流量 */
    @TableId
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
    private String attachments;

    /** partition key */
    private Integer weekOfYear;

    private Date createdAt;

    /** soft delete */
    private Date deletedAt;

    private Date done;
}
