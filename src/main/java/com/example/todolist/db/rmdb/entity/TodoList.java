package com.example.todolist.db.rmdb.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;


/**
 * 讀取用
 * 每 K 筆 todo_task 變成 1 筆 todo_list, K = 100
 */
@Data
@Accessors(chain = true)
@TableName(value = "todo_list")
@NoArgsConstructor
@AllArgsConstructor
public class TodoList implements Serializable {

    /**
     * K 筆 todo_task 的第一筆 tid
     */
    @TableId
    private Long lid;

    /**
     * 第一筆 created_at
     */
    private Date firstCreatedAt;

    /**
     * 第一筆 created_at 的 month
     * note sharding key 平衡 多庫 讀取流量
     */
    private Integer firstMonth;

    /**
     * 第一筆 created_at 的 week_of_year
     * note partition key 平衡 單庫 讀取流量
     */
    private Integer firstWeekOfYear;

    /**
     * JSONObject 欄位
     * note 有些會是已經刪除的，但這裡僅會「標記為刪除」
     * K 筆 todo_task 的
     *     todo_task.tid(PK)
     *      <and> title
     *      <and> 前半段的 content
     *      <and> week_of_year >> partition key, 增加單筆搜尋效能!!!
     *      <and> created_at
     *      <and> deleted_at (soft deleted)
     *
     * ex: 616029305360613376L
     *      <and> 今日事
     *      <and> 1. 打掃 2. 洗碗...
     *      <and> 28
     *      <and> 2021-05-30 11:22:33
     *      <and> null OR 2021-05-30 11:22:33
     */
    private String todoTasks;

//    /**
//     * K 筆 todo_task 的最後一筆 lid
//     * TODO ???add index (second index)
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
}
