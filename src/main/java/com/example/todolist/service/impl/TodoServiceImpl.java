package com.example.todolist.service.impl;

import com.example.todolist.db.rmdb.entity.TodoTask;
import com.example.todolist.db.rmdb.repo.TodoTaskRepository;
import com.example.todolist.model.bo.TodoTaskBo;
import com.example.todolist.model.vo.BatchVo;
import com.example.todolist.model.vo.TodoTaskVo;
import com.example.todolist.service.TodoService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@AllArgsConstructor
public class TodoServiceImpl implements TodoService {


    @Autowired
    private TodoTaskRepository taskRepo;

    @Override
    public Long create(TodoTaskBo todoTaskBo) {
        Date now = new Date();
        Integer weekOfYear = 1; // get week of year
        Long tid = taskRepo.insert(
                todoTaskBo.getTitle(),
                todoTaskBo.getContent(),
                todoTaskBo.getAttachments(),
                weekOfYear,
                now
        );
        log.info("create a task. tid: {}", tid);

        return tid;
    }

    /**
     * @param startTime
     * @param seq tid
     * @param limit
     * @return
     */
    @Override
    public BatchVo getList(Date startTime, String seq, Integer limit) {

        List<TodoTask> tasks;
        if (Objects.isNull(seq)) {
            log.info("hot search by time.  limit: {} startTime: {}", limit, startTime);
            tasks = taskRepo.getList(startTime, limit + 1);
        } else {
            log.info("hot search with time + seq.  limit: {} startTime: {} seq: {}", limit, startTime, seq);
            // 這裡和 HistoryListService.getList 的 seq 格式不統一
            Long tid = Long.valueOf(seq);
            tasks = taskRepo.getList(
                    startTime,
                    tid,
                    limit + 1);
        }

        if (tasks.isEmpty()) {
            log.info("hot search with empty returned.  limit: {} startTime: {} seq: {}", limit, startTime, seq);
            return new BatchVo(limit);
        }

        List<TodoTaskVo> taskVos = toTodoTaskVos(tasks);
        int lastOne = taskVos.size() - 1;
        if (lastOne == 0) {
            return new BatchVo(
                    taskVos,
                    limit,
                    taskVos.size(),
                    null
            );
        }

        TodoTaskVo lastTaskVo = taskVos.remove(lastOne);

        return new BatchVo(
                taskVos,
                limit,
                taskVos.size(),
                lastTaskVo.toNext()
            );
    }

    /**
     * @param tid PK
     * @param partitionKey weekOfYear
     * @return
     */
    @Override
    public TodoTaskVo getOne(Long tid, Integer partitionKey) {
        TodoTask task = taskRepo.findOne(tid, partitionKey);
        log.info("get a task.  TodoTask: {}", task);
        return new TodoTaskVo(task);
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

    private List<TodoTaskVo> toTodoTaskVos(List<TodoTask> tasks) {
        List<TodoTaskVo> taskVos = new ArrayList<>();
        for (TodoTask task : tasks) {
            taskVos.add(new TodoTaskVo(task));
        }

        return taskVos;
    }
}
