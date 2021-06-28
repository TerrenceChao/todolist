package com.example.todolist.service.impl;

import com.example.todolist.db.rmdb.entity.TodoTask;
import com.example.todolist.model.BatchVo;
import com.example.todolist.service.TodoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
public class TodoServiceImpl implements TodoService {

    @Override
    public void create(TodoTask todoTask) {

    }

    @Override
    public BatchVo getList(Date startTime, Integer limit) {
        return null;
    }

    /**
     * @param tid PK
     * @param partitionKey weekOfYear
     * @return
     */
    @Override
    public Object getOne(Long tid, Integer partitionKey) {
        return null;
    }

    /**
     * 暫時不實現
     * TODO 附件暫時不允許更新
     * TODO 需更新 TodoList 中的資料 (rabbitmq)
     * @param tid
     * @param todoTask
     */
    @Override
    public void update(Long tid, TodoTask todoTask) {

    }

    /**
     * 暫時不實現
     * TODO 先軟刪除，需更新 TodoList 中的資料 (rabbitmq);
     * TODO 等過期一段時間後再透過 job batch 刪除 (google drive 的刪除也是在垃圾桶待上 30 天以後才真的刪除。)
     *  >> 過期一段時間後再 ... 長期不實現
     * @param tid
     */
    @Override
    public void delete(Long tid) {

    }
}
