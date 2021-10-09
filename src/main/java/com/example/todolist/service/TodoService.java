package com.example.todolist.service;

import com.example.todolist.db.rmdb.entity.TodoTask;
import com.example.todolist.model.vo.BatchVo;
import com.example.todolist.model.vo.TodoTaskVo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public interface TodoService {

    TodoTaskVo create(String title, String content, List<MultipartFile> files) throws IOException;

    BatchVo getList(Date startTime, String tid, Integer batch);

    /**
     * @param tid PK
     * @param partitionKey weekOfYear
     * @return
     */
    TodoTaskVo getOne(Long tid, Integer partitionKey);

    void updateAttach(Long tid, Integer partitionKey, String filename, String url);

    /**
     * 暫時不實現
     * TODO 附件暫時不允許更新
     * TODO 需更新 TodoList 中的資料 (rabbitmq)
     * @param tid
     * @param todoTask
     */
    void update(Long tid, TodoTask todoTask);

    /**
     * 暫時不實現
     * TODO 先軟刪除，需更新 TodoList 中的資料 (rabbitmq);
     * TODO 等過期一段時間後再透過 job batch 刪除 (google drive 的刪除也是在垃圾桶待上 30 天以後才真的刪除。)
     *  >> 過期一段時間後再 ... 長期不實現
     * @param tid
     */
    void delete(Long tid);
}
