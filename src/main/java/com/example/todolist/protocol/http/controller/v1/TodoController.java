package com.example.todolist.protocol.http.controller.v1;

import com.example.todolist.common.Constant;
import com.example.todolist.model.vo.TodoTaskVo;
import com.example.todolist.protocol.http.response.ResponseResult;
import com.example.todolist.service.HistoryListService;
import com.example.todolist.service.AttachService;
import com.example.todolist.service.TodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/todo")
public class TodoController {

    @Autowired
    private TodoService todoService;

    @Autowired
    private AttachService attachService;

    @Autowired
    private HistoryListService historyListService;

    /**
     * RequestBody and Multipart on Spring Boot (json + attach file)
     * https://blogs.perficient.com/2020/07/27/requestbody-and-multipart-on-spring-boot/
     *
     * TODO How WebFlux works?
     *
     * TODO 每 "K" 筆(寫入流量很大怎辦???, 用 MQ 異步解決) 或是 30 secs(寫入過慢) 寫入 DB: todo_list;  >>> !?!??!?
     *  不到 "K" 筆在 redis 需要記錄 idx 1 ~ idx K (最新的 < "K" 筆)  >>> !?!??!?
     *
     * 1. create one in DB
     * 2. send attachments (async)
     * 3. if the amount of latest tasks in cache(redis) < "K" ?  >>> !?!??!?
     *      Y: read one from DB and write cache (有辦法透過 snowflake 拿到 cid ?? Y)
     *      N:  1) mark/label for these "K" tasks.
     *          2) write into todo_list; using RabbitMQ
     *          3) clear cache from ??? to latest cid if 1) is done.
     */
    @PostMapping(value = "/task", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity create(@RequestPart("title") String title, @RequestPart("content") String content, @RequestPart("files") List<MultipartFile> files) throws IOException {
        // step 1
        TodoTaskVo taskVo = todoService.create(title, content, files);

        // TODO step 2 >> async
        if (Objects.nonNull(files) && ! files.isEmpty()) {
            attachService.uploadAttach(taskVo.getTid(), taskVo.getWeekOfYear(), taskVo.getAttachments(), files);
        }

        // TODO  step 2  1).. 2).. 3)  >> async async async
        // historyListService. ...

        return ResponseResult.successPost(taskVo.getTid());
    }
    
    
    /**
     * TODO 單庫透過 week_of_year 做分表, 所以除了 tid(PK) 以外，再透過 week_of_year 針對特定 partition 查詢，
     *  如此只會查詢單庫單表
     * @param tid
     * @param weekOfYear
     * @return
     */
    @GetMapping(value = "/task/{tid}", produces = "application/json;charset=utf-8")
    public ResponseEntity getByTid(@PathVariable Long tid, @RequestParam(required = false) Integer weekOfYear) {
        return ResponseResult.successGet(todoService.getOne(tid, weekOfYear));
    }

    /**
     * TODO 讀取近期的 list 從 todoService (TodoTask)
     *  讀取歷史的 list 從 historyListService (1筆 TodoList 包含 100 筆 TodoTask)
     * @param startTime
     * @param seq (not required) if null, get the min seq of the time 
     * @param limit
     * @return
     */
    @GetMapping(value = "/list", produces = "application/json;charset=utf-8")
    public ResponseEntity getList(
            @NotBlank @RequestParam @DateTimeFormat(pattern = Constant.DATETIME_FORMAT) Date startTime,
            @NotBlank @RequestParam(required = false) String seq,
            @NotBlank @RequestParam Integer limit
    ) {

        // TODO 視情況而從不同的來源獲取 list (todoService, historyListService)
        return ResponseResult.successGet(todoService.getList(startTime, seq, limit));
    }

}
