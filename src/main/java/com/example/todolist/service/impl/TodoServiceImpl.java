package com.example.todolist.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.todolist.db.rmdb.entity.TodoTask;
import com.example.todolist.db.rmdb.repo.TodoTaskRepository;
import com.example.todolist.model.bo.TodoTaskBo;
import com.example.todolist.model.vo.BatchVo;
import com.example.todolist.model.vo.TodoTaskVo;
import com.example.todolist.service.TodoService;
import com.example.todolist.util.DatetimeUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Slf4j
@Service
@AllArgsConstructor
public class TodoServiceImpl implements TodoService {

    @Autowired
    private DatetimeUtil dateUtil;

    @Autowired
    private TodoTaskRepository taskRepo;

    @Override
    public TodoTaskVo create(String title, String content, List<MultipartFile> files) throws IOException {
        TodoTaskBo todoTaskBo = parseInput(title, content, files);
        JSONObject attachments = todoTaskBo.getAttachments();

        Date now = new Date();
        Integer weekOfYear = dateUtil.getWeekOfYear(now);

        TodoTask newTask;
        if (Objects.nonNull(attachments)) {
            newTask = taskRepo.insert(
                    todoTaskBo.getTitle(),
                    todoTaskBo.getContent(),
                    attachments.toJSONString(),
                    weekOfYear,
                    now
            );
        } else {
            newTask = taskRepo.insert(
                    todoTaskBo.getTitle(),
                    todoTaskBo.getContent(),
                    null,
                    weekOfYear,
                    now
            );
        }

        log.info("create a task. task: {}", newTask);

        return new TodoTaskVo(newTask);
    }

    /**
     * @param startTime
     * @param tid tid
     * @param limit
     * @return
     */
    @Override
    public BatchVo getList(Date startTime, String tid, Integer limit) {

        List<TodoTask> tasks;
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

        if (tasks.isEmpty()) {
            log.info("hot search with empty returned.  limit: {} startTime: {} tid: {}", limit, startTime, tid);
            return new BatchVo(limit);
        }

        List<TodoTaskVo> taskVos = toTodoTaskVos(tasks);
        int lastOne = taskVos.size() - 1;
        if (lastOne < limit - 1) {
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
        TodoTask task;
        if (Objects.isNull(partitionKey)) {
            log.info("get a task.  tid: {}", tid);
            task = taskRepo.findByTid(tid);
        } else {
            log.info("get a task with 'partition key'.  tid: {}  weekOfYear: {}", tid, partitionKey);
            task = taskRepo.findOne(tid, partitionKey);
        }
//        TODO def exception
//        if (Objects.isNull(task)) {
//            throw new Exception("");
//        }

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

    protected TodoTaskBo parseInput(String title, String content, List<MultipartFile> files) throws IOException {
        TodoTaskBo taskBo = new TodoTaskBo()
                .setTitle(title)
                .setContent(content);

        if (Objects.nonNull(files) && ! files.isEmpty()) {
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
