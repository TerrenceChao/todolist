package com.example.todolist.protocol.http.controller.v2;


import com.example.todolist.common.Constant;
import com.example.todolist.model.vo.TodoTaskVo;
import com.example.todolist.protocol.http.response.ResponseResult;
import com.example.todolist.service.AttachService;
import com.example.todolist.service.HistoryListService;
import com.example.todolist.service.TodoService;
import com.example.todolist.service.TriggerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.util.Date;
import java.util.List;


@RequiredArgsConstructor
@RequestMapping("/v2/todo")
@RestController("v2TodoController")
public class TodoController {

    @Autowired
    private TodoService todoService;

    @Autowired
    private AttachService attachService;

    @Autowired
    private HistoryListService historyListService;

    @Autowired
    private TriggerService triggerService;

    /**
     * step 1. create one task in DB
     * step 2. uploading attachments (async + message queue)
     * step 3. according condition and do transformation (async + message queue)
     *
     * @param title
     * @param content
     * @param files
     * @return
     * @throws IOException
     */
    @PostMapping(value = "/tasks", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity create(@RequestPart("title") String title, @RequestPart("content") String content, @RequestPart(value = "files", required = false) List<MultipartFile> files) throws Exception {
        // step 1
        TodoTaskVo taskVo = todoService.create(title, content, files);
        Long tid = taskVo.getTid();
        Date date = taskVo.getCreatedAt();

        // step 2 >> async + message queue
        if (taskVo.hasAttachments()) {
            attachService.uploadAttach(tid, taskVo.getWeekOfYear(), taskVo.getAttachments(), files);
        }

        // step 3 >> async + message queue
        triggerService.transformAsync(date.getTime());

        return ResponseResult.successPost(tid);
    }


    /**
     * 單庫透過 week_of_year 做分表, 所以除了 tid(PK) 以外，再透過 week_of_year
     * 針對特定 partition 查詢，如此只會查詢單庫單表
     *
     * @param tid
     * @param weekOfYear
     * @return
     */
    @GetMapping(value = "/tasks/{tid}", produces = "application/json;charset=utf-8")
    public ResponseEntity getByTid(@PathVariable Long tid, @RequestParam(required = false) Integer weekOfYear) {
        return ResponseResult.successGet(todoService.getOne(tid, weekOfYear));
    }

    /**
     * 讀取近期的 list 從 todoService (TodoTask)
     * 讀取歷史的 list 從 historyListService (1筆 TodoList 包含 K 筆 TodoTask)
     *
     * @param startTime
     * @param seq       (not required) if null, get the min seq of the time
     * @param limit
     * @return
     */
    @GetMapping(value = "/tasks", produces = "application/json;charset=utf-8")
    public ResponseEntity getList(
            @NotBlank @RequestParam @DateTimeFormat(pattern = Constant.DATETIME_FORMAT) Date startTime,
            @NotBlank @RequestParam(required = false) String seq,
            @NotBlank @RequestParam Integer limit
    ) {
        if (startTime.getTime() < triggerService.getLastTimestamp()) {
            return ResponseResult.successGet(historyListService.getList(startTime, seq, limit));
        }

        return ResponseResult.successGet(todoService.getList(startTime, seq, limit));
    }

    /**
     * 讀取近期的 list 從 todoService (TodoTask)
     * 讀取歷史的 list 從 historyListService (1筆 TodoList 包含 K 筆 TodoTask)
     *
     * @param startTimestamp
     * @param limit
     * @param seq
     * @return
     */
    @GetMapping(value = "/tasks/{startTimestamp}/{limit}", produces = "application/json;charset=utf-8")
    public ResponseEntity getList(
            @PathVariable Long startTimestamp,
            @PathVariable Integer limit,
            @NotBlank @RequestParam(required = false) String seq
    ) {
        if (startTimestamp < triggerService.getLastTimestamp()) {
            return ResponseResult.successGet(historyListService.getList(new Date(startTimestamp), seq, limit));
        }

        return ResponseResult.successGet(todoService.getList(new Date(startTimestamp), seq, limit));
    }
}

