package com.example.todolist.db.rmdb.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

/**
 * 讀取用
 * 每 10 筆 todo_task 變成 1 筆 todo_list
 */
@Data
public class TodoList {

    @TableId
    private Long lid;

    /**
     * JSONObject 欄位
     * TODO 有些會是已經刪除的，但這裡僅會「標記為刪除」
     *  10 筆 todo_task 的
     *      todo_task.tid(PK) <and>
     *      title <and>
     *      前半段的 content <and>
     *      week_of_year TODO week_of_year 是 partition key, 增加單筆搜尋效能!!!
     *
     * ex: 616029305360613376L <and>
     *     今日事 <and>
     *     1. 打掃 2. 洗碗...  <and>
     *     28
     */
    private String todoTasks;

    /**
     * 10 筆 todo_task 的第一筆 created_at
     * ??? sharding key 平衡讀取流量
     */
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
     * 10 筆 todo_task 的最後一筆 created_at
     */
    private Date lastCreatedAt;
}
