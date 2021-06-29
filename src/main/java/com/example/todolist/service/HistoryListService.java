package com.example.todolist.service;

import com.example.todolist.model.vo.BatchVo;

import java.util.Date;

public interface HistoryListService {

    BatchVo getList(Date startTime, String seq, Integer batch);
}
