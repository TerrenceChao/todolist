package com.example.todolist.service.impl;

import com.example.todolist.db.rmdb.entity.TodoList;
import com.example.todolist.db.rmdb.entity.TodoTask;
import com.example.todolist.db.rmdb.repo.TodoListRepository;
import com.example.todolist.db.rmdb.repo.TodoTaskRepository;
import com.example.todolist.model.vo.BatchVo;
import com.example.todolist.model.vo.TodoListVo;
import com.example.todolist.model.vo.TodoTaskVo;
import com.example.todolist.service.HistoryListService;
import com.example.todolist.util.DatetimeUtil;
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

    @Autowired
    private DatetimeUtil dateUtil;

    /**
     * @param tid todo_task: tid
     * @param limit TODO: 限制為 100 的倍數
     * @return
     */
    @Override
    public BatchVo transform(String tid, Integer limit) {

        log.info("hot search with tid.  limit: {} tid: {}", limit, tid);
        List<TodoTask> tasks = taskRepo.getList(Long.valueOf(tid), limit + 1);
        if (tasks.isEmpty()) {
            log.info("hot search with empty returned.  limit: {} tid: {}", limit, tid);
            return new BatchVo(limit);
        }

        //

        return null;
    }

    /**
     * @param startTime
     * @param tid
     * @param limit
     * @return
     */
    @Override
    public BatchVo getList(Date startTime, String tid, Integer limit) {
        // TODO batch 和 task size 不一樣, 這樣調整!?
        int batch = limit / 100;
        Integer month = dateUtil.getMonth(startTime);
        Integer weekOfYear = dateUtil.getWeekOfYear(startTime);

        List<TodoList> todoListCollect;
        if (Objects.isNull(tid)) {
            log.info("[history] search by time.  limit: {} startTime: {}", limit, startTime);
            todoListCollect = todoListRepo.getBatchTodoList(startTime, month, weekOfYear, batch + 1);
        } else {
            log.info("[history] search with time + tid.  limit: {} startTime: {} tid: {}", limit, startTime, tid);

            todoListCollect = todoListRepo.getBatchTodoList(
                    startTime,
                    month,
                    weekOfYear,
                    Long.valueOf(tid),
                    batch + 1
            );
        }

        if (todoListCollect.isEmpty()) {
            log.info("[history] search with empty returned.  limit: {} startTime: {} tid: {}", limit, startTime, tid);
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
