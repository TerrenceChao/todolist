package com.example.todolist.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.todolist.db.rmdb.entity.TodoTask;
import com.example.todolist.db.rmdb.repo.TodoListRepository;
import com.example.todolist.db.rmdb.repo.TodoTaskRepository;
import com.example.todolist.model.bo.TodoTaskBo;
import com.example.todolist.model.vo.BatchVo;
import com.example.todolist.model.vo.TodoListVo;
import com.example.todolist.model.vo.TodoTaskVo;
import com.example.todolist.service.TodoService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.*;

@Slf4j
@Service
@AllArgsConstructor
public class TodoServiceImpl implements TodoService {

    @Autowired
    private TodoListRepository todoListRepo;

    @Autowired
    private TodoTaskRepository taskRepo;

    @Override
    public Long create(TodoTaskBo todoTaskBo) {
//        ZonedDateTime time = ZonedDateTime().now();
        Date now = new Date();
        Integer weekOfYear = 1; // get week of year
        return taskRepo.insert(
                todoTaskBo.getTitle(),
                todoTaskBo.getContent(),
                todoTaskBo.getAttachments(),
                weekOfYear,
                now.toString()
        );
    }

    /**
     * @param startTime
     * @param seq
     * @param batch
     * @return
     */
    @Override
    public BatchVo getList(ZonedDateTime startTime, String seq, Integer batch) {
        List<TodoListVo> vos = Objects.isNull(seq) ?
                todoListRepo.getList(startTime, batch + 1) :
                todoListRepo.getList(startTime, seq, batch + 1);
        if (vos.isEmpty()) {
            return new BatchVo(batch);
        }

        JSONObject next = vos.remove(vos.size() - 1).toNext();
        List<TodoTaskVo> taskVos = mergeTasks(vos);
        return new BatchVo(
                vos,
                batch, // TODO batch 和 task size 不一樣, 調整!?
                taskVos.size(),
                next
            );
    }

    /**
     * @param tid PK
     * @param partitionKey weekOfYear
     * @return
     */
    @Override
    public TodoTaskVo getOne(Long tid, Integer partitionKey) {
        return taskRepo.findOne(tid, partitionKey);
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

    private List<TodoTaskVo> mergeTasks(List<TodoListVo> listVos) {
        List<TodoTaskVo> tasks = new ArrayList<>();
        for (TodoListVo listVo : listVos) {
            tasks.addAll(listVo.toTaskList());
        }

        return tasks;
    }
}
