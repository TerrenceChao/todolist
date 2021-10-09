package com.example.todolist.service;

import com.example.todolist.model.vo.BatchVo;

import java.util.Date;

public interface HistoryListService {

    /**
     *
     * @param tid todo_task: tid
     * @param limit
     * @return
     */
    BatchVo transform(String tid, Integer limit);

    BatchVo getList(Date startTime, String tid, Integer limit);
}
