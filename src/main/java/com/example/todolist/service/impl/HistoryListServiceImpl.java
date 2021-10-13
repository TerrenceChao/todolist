package com.example.todolist.service.impl;

import com.alibaba.fastjson.JSONArray;
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
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@AllArgsConstructor
public class HistoryListServiceImpl implements HistoryListService {

    @Autowired
    private Environment env;

    @Autowired
    private TodoListRepository todoListRepo;

    @Autowired
    private TodoTaskRepository taskRepo;

    @Autowired
    private DatetimeUtil dateUtil;

    /**
     * @param tid todo_task: tid
     * @param limit TODO: 限制為 10 的倍數
     * @return
     */
    @Override
    public BatchVo transform(String tid, Integer limit) {

        log.info("hot search with tid.  limit: {} tid: {}", limit, tid);
        List<TodoTask> tasks = taskRepo.getList(Long.valueOf(tid), limit + 1);
        if (tasks.isEmpty()) {
            log.info("the transformation returns empty.  limit: {} tid: {}", limit, tid);
            return new BatchVo(limit);
        }

        TodoTask firstTask = tasks.get(0);
        int firstMonth = dateUtil.getMonth(firstTask.getCreatedAt());
        int contentPrefixLen = Integer.parseInt(Objects.requireNonNullElse(env.getProperty("todo-task.content.preview-length"), "10"));
        TodoList todoList = generateTodoList(tasks, firstMonth, contentPrefixLen);
        todoListRepo.insert(todoList);

        TodoListVo listVo = new TodoListVo(todoList);
        List<TodoTaskVo> taskVos = mergeTodoTaskVos(new ArrayList(){{ add(listVo); }});
        int lastOne = taskVos.size() - 1;
        if (lastOne < limit - 1) {
            return new BatchVo(
                    taskVos,
                    limit,
                    taskVos.size(),
                    null
            );
        }

        taskVos.remove(lastOne);
        return new BatchVo(
                taskVos,
                limit,
                taskVos.size(),
                listVo.toNext()
        );
    }

    /**
     * @param startTime
     * @param tid
     * @param limit
     * @return
     */
    @Override
    public BatchVo getList(Date startTime, String tid, Integer limit) {
        if (exceedMaxLimit(limit)) {
            // TODO define exception
            return new BatchVo(limit);
        }

        Integer month = dateUtil.getMonth(startTime);
        Integer weekOfYear = dateUtil.getWeekOfYear(startTime);

        List<TodoList> todoListCollect;
        if (Objects.isNull(tid)) {
            log.info("[history] search by time.  limit: {} startTime: {}", limit, startTime);
            todoListCollect = todoListRepo.getBatchTodoList(startTime, month, weekOfYear, 2);
        } else {
            log.info("[history] search with time + tid.  limit: {} startTime: {} tid: {}", limit, startTime, tid);
            todoListCollect = todoListRepo.getBatchTodoList(
                    startTime,
                    month,
                    weekOfYear,
                    Long.valueOf(tid),
                    2
            );
        }

        if (todoListCollect.isEmpty()) {
            log.info("[history] search with empty returned.  limit: {} startTime: {} tid: {}", limit, startTime, tid);
            return new BatchVo(limit);
        }

        long firstTid = Long.valueOf(tid);
        List<TodoListVo> todoListVos = toTodoListVos(todoListCollect);
        List<TodoTaskVo> taskVos = mergeTodoTaskVos(todoListVos);
        taskVos = filterTodoTaskVos(taskVos, firstTid, limit + 1);

        int lastOne = taskVos.size() - 1;
        if (lastOne < limit - 1) {
            return new BatchVo(
                    taskVos,
                    limit,
                    taskVos.size(),
                    null
            );
        }

        TodoTaskVo lastVo = taskVos.remove(lastOne);
        return new BatchVo(
                taskVos,
                limit,
                taskVos.size(),
                lastVo.toNext()
        );
    }

    private TodoList generateTodoList(List<TodoTask> tasks, int firstMonth, int contentPrefixLen) {
        TodoTask firstTask = tasks.get(0);

        JSONArray jsonArray = new JSONArray();
        for (TodoTask task : tasks) {
            jsonArray.add(task.toJSON(contentPrefixLen));
        }
        String todoTasks = jsonArray.toJSONString();

        int nextBatchIdx = tasks.size() - 1;
        TodoTask nextBatchTask = tasks.get(nextBatchIdx);

        return new TodoList(
                firstTask.getTid(),
                firstTask.getCreatedAt(),
                firstMonth,
                firstTask.getWeekOfYear(),
                todoTasks,
                nextBatchTask.getTid(),
                nextBatchTask.getCreatedAt()
        );
    }

    private boolean exceedMaxLimit(Integer limit) {
        String max = Objects.requireNonNull(env.getProperty("todo-list.max"));
        return Integer.parseInt(max) < limit;
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

    /**
     * 透過二元搜尋加速
     * @param taskVos
     * @param targetTid
     * @param limit
     * @return
     */
    private List<TodoTaskVo> filterTodoTaskVos(List<TodoTaskVo> taskVos, long targetTid, int limit) {
        TodoTaskVo task;
        int srt = 0,
            end = taskVos.size() - 1,
            mid,
            fromIdx = 0;

        while (srt < end) {
            mid = srt + (end - srt) / 2;
            task = taskVos.get(mid);

            long midTid = task.getTid();
            if (targetTid < midTid) {
                end = mid;
                continue;
            }

            fromIdx = mid;
            if (midTid < targetTid) {
                srt = mid;
            } else {
                break;
            }
        }

        int toIndex = Math.min(taskVos.size(), fromIdx + limit);
        return taskVos.subList(fromIdx, toIndex);
    }
}
