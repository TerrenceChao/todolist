package com.example.todolist.service;

import com.example.todolist.model.vo.BatchVo;

import java.util.Date;

public interface HistoryListService {

    /**
     * TODO 補償機制 由外部 scheduler 觸發
     * @param limit
     * @return
     */
    BatchVo transform(Integer limit);

    /**
     *
     * @param firstTime
     * @param limit
     * @return
     */
    BatchVo transform(Date firstTime, Integer limit);

    /**
     *
     * @param tid todo_task: tid
     * @param limit
     * @return
     */
    BatchVo transform(String tid, Integer limit);


    BatchVo getList(Date startTime, String tid, Integer limit);
}
