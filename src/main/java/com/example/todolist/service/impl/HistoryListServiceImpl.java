package com.example.todolist.service.impl;

import com.example.todolist.db.rmdb.entity.TodoList;
import com.example.todolist.db.rmdb.entity.TodoTask;
import com.example.todolist.db.rmdb.repo.TodoListRepository;
import com.example.todolist.db.rmdb.repo.TodoTaskRepository;
import com.example.todolist.model.vo.BatchVo;
import com.example.todolist.model.vo.TodoSeqVo;
import com.example.todolist.model.vo.TodoListVo;
import com.example.todolist.model.vo.TodoTaskVo;
import com.example.todolist.service.HistoryListService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@AllArgsConstructor
public class HistoryListServiceImpl implements HistoryListService {

    @Autowired
    private TodoListRepository todoListRepo;

    @Autowired
    private TodoTaskRepository taskRepo;

    /**
     * @param seq
     * @param limit TODO: 限制為 100 的倍數
     * @return
     */
    @Override
    public BatchVo transform(String seq, Integer limit) {

        log.info("hot search with time + seq.  limit: {} seq: {}", limit, seq);
        // 這裡和 HistoryListService.getList 的 seq 格式不統一
        Long tid = Long.valueOf(seq);
        List<TodoTask> tasks = taskRepo.getList(tid, limit + 1);
        if (tasks.isEmpty()) {
            log.info("hot search with empty returned.  limit: {} seq: {}", limit, seq);
            return new BatchVo(limit);
        }



        return null;
    }

    /**
     * @param startTime
     * @param seq
     * @param limit
     * @return
     */
    @Override
    public BatchVo getList(Date startTime, String seq, Integer limit) {
        // TODO batch 和 task size 不一樣, 這樣調整!?
        int batch = limit / 100;

        List<TodoList> todoListCollect;
        if (Objects.isNull(seq)) {
            log.info("[history] search by time.  limit: {} startTime: {}", limit, startTime);
            todoListCollect = todoListRepo.getBatchTodoList(startTime, batch + 1);
        } else {
            log.info("[history] search with time + seq.  limit: {} startTime: {} seq: {}", limit, startTime, seq);
            TodoSeqVo seqVo = TodoListVo.parseSeq(seq);
            todoListCollect = todoListRepo.getBatchTodoList(
                    startTime,
                    seqVo.getMonth(),
                    seqVo.getWeekOfYear(),
                    seqVo.getLid(),
                    batch + 1);
        }

        if (todoListCollect.isEmpty()) {
            log.info("[history] search with empty returned.  limit: {} startTime: {} seq: {}", limit, startTime, seq);
            return new BatchVo(limit);
        }

        List<TodoListVo> todoListVos = toTodoListVos(todoListCollect);
        int lastBatch = todoListVos.size() - 1;
        TodoListVo lastVo = todoListVos.remove(lastBatch);
        List<TodoTaskVo> taskVos = mergeTodoTaskVos(todoListVos);

        return new BatchVo(
                taskVos,
                limit,
                taskVos.size(),
                lastVo.toNext()
        );
    }


    private List<TodoListVo> toTodoListVos(List<TodoList> TodoListCollect) {
        List<TodoListVo> todoListVos = new ArrayList<>();
        for (TodoList list : TodoListCollect) {
            todoListVos.add(new TodoListVo(list));
        }

        return todoListVos;
    }

    private List<TodoTaskVo> mergeTodoTaskVos(List<TodoListVo> listVos) {
        List<TodoTaskVo> tasks = new ArrayList<>();
        for (TodoListVo listVo : listVos) {
            tasks.addAll(listVo.toTaskVos());
        }

        return tasks;
    }
}
