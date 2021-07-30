package com.example.todolist.service;

import com.example.todolist.model.vo.BatchVo;

import java.util.Date;

public interface HistoryListService {

    /**
     *
     * @param seq todo_task: tid
     * @param limit
     * @return
     */
    BatchVo transform(String seq, Integer limit);

    BatchVo getList(Date startTime, String seq, Integer limit);
}
