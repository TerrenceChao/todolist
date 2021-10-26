package com.example.todolist.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.todolist.common.ResponseCode;
import com.example.todolist.common.exception.CreationException;
import com.example.todolist.common.exception.ReqFormatException;
import com.example.todolist.common.exception.ReqNotFoundException;
import com.example.todolist.common.exception.SearchException;
import com.example.todolist.db.rmdb.entity.TodoTask;
import com.example.todolist.db.rmdb.repo.TodoTaskRepository;
import com.example.todolist.model.bo.TodoTaskBo;
import com.example.todolist.model.vo.BatchVo;
import com.example.todolist.model.vo.TodoTaskVo;
import com.example.todolist.service.TodoService;
import com.example.todolist.util.DatetimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Slf4j
@Service
public class TodoServiceImpl implements TodoService {

    @Autowired
    private DatetimeUtil dateUtil;

    @Autowired
    private TodoTaskRepository taskRepo;

    private int min;

    private int max;

    public TodoServiceImpl(DatetimeUtil dateUtil, TodoTaskRepository taskRepo, Environment env) {
        this.dateUtil = dateUtil;
        this.taskRepo = taskRepo;

        String minStr = Objects.requireNonNullElse(env.getProperty("todo-task.min"), "10");
        min = Integer.parseInt(minStr);

        String maxStr = Objects.requireNonNullElse(env.getProperty("todo-task.max"), "100");
        max = Integer.parseInt(maxStr);
    }

    @Override
    public TodoTaskVo create(String title, String content, List<MultipartFile> files) throws IOException {
        TodoTaskBo todoTaskBo = parseInput(title, content, files);
        JSONObject attachments = todoTaskBo.getAttachments();

        Date now = new Date();
        Integer weekOfYear = dateUtil.getWeekOfYear(now);

        TodoTask newTask;
        try {
            newTask = taskRepo.insert(
                    todoTaskBo.getTitle(),
                    todoTaskBo.getContent(),
                    Objects.nonNull(attachments) ? attachments.toJSONString() : null,
                    weekOfYear,
                    now
            );
        } catch (Exception e) {
            log.error("task creation error", e.getStackTrace());
            throw new CreationException(ResponseCode.TASK_CREATION_ERROR);
        }

        log.info("create a task. task: {}", newTask);

        return new TodoTaskVo(newTask);
    }

    /**
     * @param startTime
     * @param tid
     * @param limit
     * @return
     */
    @Override
    public BatchVo getList(Date startTime, String tid, Integer limit) {
        if (lessThanMin(limit) || exceedMaxLimit(limit)) {
            throw new ReqFormatException("limit is out of range");
        }

        List<TodoTask> tasks;
        try {
            if (Objects.isNull(tid)) {
                log.info("hot search by time.  limit: {} startTime: {}", limit, startTime);
                tasks = taskRepo.getList(startTime, limit + 1);
            } else {
                log.info("hot search with time + tid.  limit: {} startTime: {} tid: {}", limit, startTime, tid);
                tasks = taskRepo.getList(
                        startTime,
                        Long.parseLong(tid),
                        limit + 1);
            }
        } catch (Exception e) {
            log.error("task search error", e.getStackTrace());
            throw new SearchException(ResponseCode.TASK_SEARCH_ERROR);
        }

        if (tasks.isEmpty()) {
            log.info("hot search with empty returned.  limit: {} startTime: {} tid: {}", limit, startTime, tid);
            return new BatchVo(limit);
        }

        List<TodoTaskVo> taskVos = toTodoTaskVos(tasks);
        return new BatchVo(
                taskVos,
                limit,
                taskVos.size(),
                getNextTask(taskVos, limit)
        );
    }

    /**
     * @param tid          PK
     * @param partitionKey weekOfYear
     * @return
     */
    @Override
    public TodoTaskVo getOne(Long tid, Integer partitionKey) {
        TodoTask task;
        try {
            if (Objects.isNull(partitionKey)) {
                log.info("get a task.  tid: {}", tid);
                task = taskRepo.findByTid(tid);
            } else {
                log.info("get a task with 'partition key'.  tid: {}  weekOfYear: {}", tid, partitionKey);
                task = taskRepo.findOne(tid, partitionKey);
            }

        } catch (Exception e) {
            log.error("task search error", e.getStackTrace());
            throw new SearchException(ResponseCode.TASK_SEARCH_ERROR);
        }

        if (Objects.isNull(task)) {
            throw new ReqNotFoundException(ResponseCode.TASK_NOT_FOUND, " tid: " + tid + " weekOfYear: " + partitionKey);
        }

        return new TodoTaskVo(tid, task);
    }

    @Override
    @Transactional
    public void updateAttach(Long tid, Integer partitionKey, String filename, String url) {
        // TODO update todo_task.attachments
    }

    /**
     * 暫時不實現
     * TODO 附件暫時不允許更新
     * TODO 需更新 TodoList 中的資料 (rabbitmq)
     *
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
     * >> 過期一段時間後再 ... 長期不實現
     *
     * @param tid
     */
    @Override
    public void delete(Long tid) {

    }

    private boolean lessThanMin(Integer limit) {
        return limit < min;
    }

    private boolean exceedMaxLimit(Integer limit) {
        return max < limit;
    }

    private JSONObject getNextTask(List<TodoTaskVo> taskVos, int limit) {
        int lastOne = taskVos.size() - 1;
        return (lastOne <= limit - 1) ? null : taskVos.remove(lastOne).toNext();
    }

    protected TodoTaskBo parseInput(String title, String content, List<MultipartFile> files) throws IOException {
        TodoTaskBo taskBo = new TodoTaskBo()
                .setTitle(title)
                .setContent(content);

        if (Objects.nonNull(files) && !files.isEmpty()) {
            JSONObject attachments = toAttachments(files);
            taskBo.setAttachments(attachments);
        }

        return taskBo;
    }

    protected JSONObject toAttachments(List<MultipartFile> files) throws IOException {
        JSONObject attachments = new JSONObject();

        log.info("Composite attachments' JSON");
        for (MultipartFile file : files) {
            JSONObject attach = toAttach(file);
            if (Objects.nonNull(attach)) {
                attachments.put(file.getOriginalFilename(), attach);
            }
        }

        log.info("Composite attachments' JSON. attachments: {}", attachments);

        return attachments;
    }

    protected JSONObject toAttach(MultipartFile file) throws IOException {
        if (file.getSize() == 0) {
            return null;
        }

        JSONObject attach = new JSONObject();
        String hashcode = DigestUtils.md5Hex(file.getBytes());

        attach.put("hash", hashcode);
        attach.put("size", file.getSize() + "B");

        return attach;
    }

    protected List<TodoTaskVo> toTodoTaskVos(List<TodoTask> tasks) {
        List<TodoTaskVo> taskVos = new ArrayList<>();
        for (TodoTask task : tasks) {
            taskVos.add(new TodoTaskVo(task));
        }

        return taskVos;
    }
}
