package com.example.todolist.db.rmdb.entity;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

/**
 * 讀取用
 * 每 100 筆 todo_task 變成 1 筆 todo_list
 */
@Data
public class TodoList {

    /**
     * 100 筆 todo_task 的第一筆 tid
     */
    @TableId
    private Long lid;

    /**
     * JSONObject 欄位
     * TODO 有些會是已經刪除的，但這裡僅會「標記為刪除」
     * 100 筆 todo_task 的
     *     todo_task.tid(PK)
     *      <and> title
     *      <and> 前半段的 content
     *      <and> week_of_year TODO week_of_year 是 partition key, 增加單筆搜尋效能!!!
     *      <and> soft_deleted
     *
     * ex: 616029305360613376L
     *      <and> 今日事
     *      <and> 1. 打掃 2. 洗碗...
     *      <and> 28
     *      <and> null OR 2021-05-30 11:22:33
     */
    private String todoTasks;

    /**
     * 第一筆 created_at
     */
    private Date firstCreatedAt;

    /**
     * 第一筆 created_at 的 month
     * TODO sharding key 平衡 多庫 讀取流量
     */
    private Integer firstMonth;

    /**
     * 第一筆 created_at 的 week_of_year
     * TODO partition key 平衡 單庫 讀取流量
     */
    private Integer firstWeekOfYear;

    /**
     * 100 筆 todo_task 的最後一筆 created_at
     */
    private Date lastCreatedAt;

    /**
     * "下一批" 100 筆 todo_task 的第一筆 tid
     */
    private Long nextLid;

    /**
     * "下一批" 100 筆 todo_task 的第一筆 created_at 的 month
     * TODO sharding key 平衡 多庫 讀取流量
     */
    private Integer nextMonth;

    /**
     * "下一批" 100 筆 todo_task 的第一筆 created_at 的 week_of_year
     */
    private String nextWeekOfYear;

}
