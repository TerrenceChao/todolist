package com.example.todolist.db.rmdb.entity;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * 寫入用
 */
@Data
@Accessors(chain = true)
@TableName(value = "todo_task")
public class TodoTask implements Serializable {

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
     * {
     *      nameA:  { hash, size },
     *      nameB:  { hash, size },
     *      ...
     * }
     */
    private String attachments;

    /** partition key */
    private Integer weekOfYear;

    private Date createdAt;

    /** soft delete */
    private Date deletedAt;

    private Date done;

    public JSONObject toJSON(int contentPrefixLen) {
        JSONObject json = new JSONObject();
        json.put("tid", tid);
        json.put("title", title);

        if (! StringUtils.isEmpty(content)) {
            if (content.length() <= contentPrefixLen) {
                json.put("content", content);
            } else {
                int len = Math.min(content.length(), contentPrefixLen);
                json.put("content", content.substring(0, len) + "...");
            }
        }

        json.put("weekOfYear", weekOfYear);
        json.put("createdAt", createdAt);
        json.put("deletedAt", deletedAt);

        return json;
    }
}
