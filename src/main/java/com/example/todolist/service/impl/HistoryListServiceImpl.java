package com.example.todolist.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.todolist.common.ResponseCode;
import com.example.todolist.common.exception.CreationException;
import com.example.todolist.common.exception.ReqFormatException;
import com.example.todolist.common.exception.SearchException;
import com.example.todolist.db.rmdb.entity.TodoList;
import com.example.todolist.db.rmdb.entity.TodoTask;
import com.example.todolist.db.rmdb.repo.TodoListRepository;
import com.example.todolist.db.rmdb.repo.TodoTaskRepository;
import com.example.todolist.model.vo.BatchVo;
import com.example.todolist.model.vo.TodoListVo;
import com.example.todolist.model.vo.TodoTaskVo;
import com.example.todolist.service.HistoryListService;
import com.example.todolist.util.DatetimeUtil;
import lombok.extern.slf4j.Slf4j;
//import org.apache.shardingsphere.transaction.annotation.ShardingTransactionType;
//import org.apache.shardingsphere.transaction.core.TransactionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
public class HistoryListServiceImpl implements HistoryListService {

    @Autowired
    private TodoListRepository todoListRepo;

    @Autowired
    private TodoTaskRepository taskRepo;

    @Autowired
    private DatetimeUtil dateUtil;

    private int min;

    private int max;

    private int contentPrefixLen;

    public HistoryListServiceImpl(
            TodoListRepository todoListRepo,
            TodoTaskRepository taskRepo,
            DatetimeUtil dateUtil,
            Environment env) {
        this.todoListRepo = todoListRepo;
        this.taskRepo = taskRepo;
        this.dateUtil = dateUtil;

        String minStr = Objects.requireNonNullElse(env.getProperty("todo-task.min"), "10");
        min = Integer.parseInt(minStr);

        String maxStr = Objects.requireNonNullElse(env.getProperty("todo-task.max"), "100");
        max = Integer.parseInt(maxStr);

        contentPrefixLen = Integer.parseInt(Objects.requireNonNullElse(env.getProperty("todo-task.content.preview-length"), "10"));
    }

    /**
     * TODO 補償機制 由外部 scheduler 觸發
     *
     * @param limit
     * @return
     */
    @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
//    @ShardingTransactionType(TransactionType.LOCAL)
    @Override
    public BatchVo transform(Integer limit) {
        if (lessThanMin(limit) || exceedMaxLimit(limit)) {
            throw new ReqFormatException("limit is out of range");
        }

        int limitPlusOne = limit + 1;
        List<TodoTask> tasks;
        TodoList latestOne;
        try {
            latestOne = todoListRepo.getLatestOne();
        } catch (Exception e) {
            log.error("todo-list search error", e.getStackTrace());
            throw new SearchException(ResponseCode.TODOLIST_SEARCH_ERROR);
        }

        try {
            if (Objects.isNull(latestOne)) {
                tasks = taskRepo.getList(limitPlusOne);
                if (tasks.size() < limitPlusOne) {
                    log.warn("The todo-tasks amount is not enough.  limit: {}", limit);
                    return new BatchVo(limit);
                }
            } else {
                Long latestLid = latestOne.getNextLid();
                tasks = taskRepo.getList(latestLid, limitPlusOne);
                if (tasks.size() < limitPlusOne) {
                    log.warn("The todo-tasks amount is not enough.  limit: {} latestLid: {}", limit, latestLid);
                    return new BatchVo(limit);
                }
            }
        } catch (Exception e) {
            log.error("todo-task search error", e.getStackTrace());
            throw new SearchException(ResponseCode.TASK_SEARCH_ERROR);
        }

        TodoList todoList = generateTodoList(tasks, contentPrefixLen, dateUtil);
        try {
            todoListRepo.insert(todoList);
        } catch (Exception e) {
            log.error("todo-list creation error", e.getStackTrace());
            throw new CreationException(ResponseCode.TODOLIST_CREATION_ERROR);
        }

        return toBatch(todoList, limit);
    }


    @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
//    @ShardingTransactionType(TransactionType.LOCAL)
    @Override
    public BatchVo transform(Date startTime, Integer limit) {
        if (lessThanMin(limit) || exceedMaxLimit(limit)) {
            throw new ReqFormatException("limit is out of range");
        }

        int limitPlusOne = limit + 1;
        List<TodoTask> tasks;

        Integer month = dateUtil.getMonth(startTime);
        Integer weekOfYear = dateUtil.getWeekOfYear(startTime);
        TodoList latestOne;
        try {
            latestOne = todoListRepo.getLatestOne(startTime, month, weekOfYear);
        } catch (Exception e) {
            log.error("todo-list search error", e.getStackTrace());
            throw new SearchException(ResponseCode.TODOLIST_SEARCH_ERROR);
        }

        try {
            if (Objects.isNull(latestOne)) {
                tasks = taskRepo.getList(startTime, limitPlusOne);
                if (tasks.size() < limitPlusOne) {
                    log.warn("The todo-tasks amount is not enough.  limit: {}", limit);
                    return new BatchVo(limit);
                }

            } else {
                Long latestLid = latestOne.getNextLid();
                tasks = taskRepo.getList(latestLid, limitPlusOne);
                if (tasks.size() < limitPlusOne) {
                    log.warn("The todo-tasks amount is not enough.  limit: {} latestLid: {}", limit, latestLid);
                    return new BatchVo(limit);
                }
            }
        } catch (Exception e) {
            log.error("todo-task search error", e.getStackTrace());
            throw new SearchException(ResponseCode.TASK_SEARCH_ERROR);
        }

        TodoList todoList = generateTodoList(tasks, contentPrefixLen, dateUtil);
        try {
            todoListRepo.insert(todoList);
        } catch (Exception e) {
            log.error("todo-list creation error", e.getStackTrace());
            throw new CreationException(ResponseCode.TODOLIST_CREATION_ERROR);
        }

        return toBatch(todoList, limit);
    }

    /**
     * @param tid   todo_task: tid
     * @param limit 限制為 10 的倍數
     * @return
     */
    @Override
    public BatchVo transform(String tid, Integer limit) {
        if (lessThanMin(limit) || exceedMaxLimit(limit)) {
            throw new ReqFormatException("limit is out of range");
        }

        log.info("transformation.  limit: {} tid: {}", limit, tid);
        int limitPlusOne = limit + 1;
        List<TodoTask> tasks;
        try {
            tasks = taskRepo.getList(Long.parseLong(tid), limitPlusOne);
            if (tasks.size() < limitPlusOne) {
                log.warn("The todo-tasks amount is not enough.  limit: {} tid: {}", limit, tid);
                return new BatchVo(limit);
            }
        } catch (Exception e) {
            log.error("todo-task search error", e.getStackTrace());
            throw new SearchException(ResponseCode.TASK_SEARCH_ERROR);
        }

        TodoList todoList = generateTodoList(tasks, contentPrefixLen, dateUtil);
        try {
            todoListRepo.insert(todoList);
        } catch (Exception e) {
            log.error("todo-list creation error", e.getStackTrace());
            throw new CreationException(ResponseCode.TODOLIST_CREATION_ERROR);
        }

        return toBatch(todoList, limit);
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
            throw new ReqFormatException("limit is out of range");
        }

        Integer month = dateUtil.getMonth(startTime);
        Integer weekOfYear = dateUtil.getWeekOfYear(startTime);
        List<TodoList> todoListCollect;
        try {
            if (Objects.isNull(tid)) {
                log.info("[history] search by time.  limit: {} startTime: {}", limit, startTime);
                todoListCollect = todoListRepo.getBatchTodoList(startTime, month, weekOfYear, 2);
            } else {
                log.info("[history] search with time + tid.  limit: {} startTime: {} tid: {}", limit, startTime, tid);
                todoListCollect = todoListRepo.getBatchTodoList(
                        startTime,
                        month,
                        weekOfYear,
                        Long.parseLong(tid),
                        2
                );
            }
        } catch (Exception e) {
            log.error("todo-list search error", e.getStackTrace());
            throw new SearchException(ResponseCode.TODOLIST_SEARCH_ERROR);
        }

        if (todoListCollect.isEmpty()) {
            log.warn("[history] search with empty returned.  limit: {} startTime: {} tid: {}", limit, startTime, tid);
            return new BatchVo(limit);
        }

        List<TodoListVo> todoListVos = toTodoListVos(todoListCollect);
        List<TodoTaskVo> taskVos = mergeTodoTaskVos(todoListVos);
        taskVos = filterTodoTaskVos(taskVos, startTime, tid, limit + 1);

        return new BatchVo(
                taskVos,
                limit,
                taskVos.size(),
                getNextTask(taskVos, limit)
        );
    }

    private boolean lessThanMin(Integer limit) {
        return limit < min;
    }

    private BatchVo<TodoTaskVo> toBatch(TodoList todoList, int limit) {
        TodoListVo listVo = new TodoListVo(todoList);
        List<TodoTaskVo> taskVos = mergeTodoTaskVos(new ArrayList() {{
            add(listVo);
        }});
        return new BatchVo(
                taskVos,
                limit,
                taskVos.size(),
                listVo.toNext()
        );
    }

    private TodoList generateTodoList(List<TodoTask> tasks, int contentPrefixLen, DatetimeUtil dateUtil) {
        TodoTask firstTask = tasks.get(0);

        // 移除最後一個，當下一批次的第一筆 TodoTask
        int nextBatchIdx = tasks.size() - 1;
        TodoTask nextBatchTask = tasks.remove(nextBatchIdx);

        JSONArray jsonArray = new JSONArray();
        for (TodoTask task : tasks) {
            jsonArray.add(task.toJSON(contentPrefixLen));
        }
        String todoTasks = jsonArray.toJSONString();

        return new TodoList(
                firstTask.getTid(),
                firstTask.getCreatedAt(),
                dateUtil.getMonth(firstTask.getCreatedAt()),
                firstTask.getWeekOfYear(),
                todoTasks,
                nextBatchTask.getTid(),
                nextBatchTask.getCreatedAt()
        );
    }

    private boolean exceedMaxLimit(Integer limit) {
        return max < limit;
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
     * @param taskVos
     * @param startTime
     * @param firstTid
     * @param limit
     * @return
     */
    private List<TodoTaskVo> filterTodoTaskVos(List<TodoTaskVo> taskVos, Date startTime, String firstTid, int limit) {
        int startIdx;
        if (Objects.nonNull(firstTid)) {
            long targetTid = Long.parseLong(firstTid);
            startIdx = getStartIdxById(taskVos, targetTid);
        } else {
            long targetTime = startTime.getTime();
            startIdx = getStartIdxByTimestamp(taskVos, targetTime);
        }

        int endIdx = Math.min(taskVos.size(), startIdx + limit);
        return taskVos.subList(startIdx, endIdx);
    }

    private JSONObject getNextTask(List<TodoTaskVo> taskVos, int limit) {
        int lastOne = taskVos.size() - 1;
        return (lastOne <= limit - 1) ? null : taskVos.remove(lastOne).toNext();
    }

    /**
     * Using Binary Search
     *
     * @param taskVos
     * @param targetTid
     * @return
     */
    private int getStartIdxById(List<TodoTaskVo> taskVos, long targetTid) {
        TodoTaskVo task;
        int srt = 0,
                end = taskVos.size() - 1,
                mid,
                startIdx = 0;

        while (srt + 1 < end) {
            mid = srt + (end - srt) / 2;
            task = taskVos.get(mid);

            long midTid = task.getTid();
            if (targetTid < midTid) {
                end = mid;
                continue;
            }

            startIdx = mid;
            if (midTid < targetTid) {
                srt = mid;
            } else {
                break;
            }
        }

        return startIdx;
    }

    /**
     * Using Binary Search
     *
     * @param taskVos
     * @param targetTime
     * @return
     */
    private int getStartIdxByTimestamp(List<TodoTaskVo> taskVos, long targetTime) {
        TodoTaskVo task;
        int srt = 0,
                end = taskVos.size() - 1,
                mid,
                startIdx = 0;

        while (srt + 1 < end) {
            mid = srt + (end - srt) / 2;
            task = taskVos.get(mid);

            long midTime = task.getCreatedAt().getTime();
            if (targetTime < midTime) {
                end = mid;
                continue;
            }

            startIdx = mid;
            if (midTime < targetTime) {
                srt = mid;
            } else {
                break;
            }
        }

        return startIdx;
    }
}
