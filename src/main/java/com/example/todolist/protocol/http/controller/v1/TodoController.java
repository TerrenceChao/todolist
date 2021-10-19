package com.example.todolist.protocol.http.controller.v1;

import com.example.todolist.common.Constant;
import com.example.todolist.model.vo.TodoTaskVo;
import com.example.todolist.protocol.http.response.ResponseResult;
import com.example.todolist.service.HistoryListService;
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

@RequiredArgsConstructor
@RequestMapping("/v1/todo")
@RestController("v1TodoController")
public class TodoController {

    @Autowired
    private TodoService todoService;

    @Autowired
    private HistoryListService historyListService;

    /**
     * RequestBody and Multipart on Spring Boot (json + attach file)
     * https://blogs.perficient.com/2020/07/27/requestbody-and-multipart-on-spring-boot/
     *
     * TODO 1) How WebFlux works?
     *
     * TODO 2) 每 "K" 筆(寫入流量很大怎辦???, 用 MQ 異步解決) 或是 30 secs(寫入過慢) 寫入 DB: todo_list;  >>> !?!??!?
     *  不到 "K" 筆在 redis 需要記錄 idx 1 ~ idx K (最新的 < "K" 筆)  >>> !?!??!?
     *  1. create one in DB
     *  2. send attachments (async)
     *  3. if the amount of latest tasks in cache(redis) < "K" ?  >>> !?!??!?
     *      Y: read one from DB and write cache
     *      N:  1) mark/label for these "K" tasks.
     *          2) write into todo_list; using RabbitMQ
     *          3) clear cache from ??? to latest cid if 1) is done.
     */
    @PostMapping(value = "/tasks", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity create(@RequestPart("title") String title, @RequestPart("content") String content, @RequestPart("files") List<MultipartFile> files) throws IOException {
        TodoTaskVo taskVo = todoService.create(title, content, files);

//        // step 2 >> async + message queue
//        if (taskVo.hasAttachments()) {
//            attachService.uploadAttach(tid, taskVo.getWeekOfYear(), taskVo.getAttachments(), files);
//        }
//
//        // step 3 >> async + message queue
//        if (transformService.needTransform(tid)) {
//            transformService.sendMsg(tid);
//        }

        return ResponseResult.successPost(taskVo.getTid());
    }

    /**
     * 單庫透過 week_of_year 做分表, 所以除了 tid(PK) 以外，再透過 week_of_year
     * 針對特定 partition 查詢，如此只會查詢單庫單表
     * @param tid
     * @param weekOfYear
     * @return
     */
    @GetMapping(value = "/tasks/{tid}", produces = "application/json;charset=utf-8")
    public ResponseEntity getByTid(@PathVariable Long tid, @RequestParam(required = false) Integer weekOfYear) {
        return ResponseResult.successGet(todoService.getOne(tid, weekOfYear));
    }

    /**
     * @param startTime
     * @param seq (not required) if null, get the min seq of the time
     * @param limit
     * @return
     */
    @GetMapping(value = "/tasks", produces = "application/json;charset=utf-8")
    public ResponseEntity getList(
            @NotBlank @RequestParam @DateTimeFormat(pattern = Constant.DATETIME_FORMAT) Date startTime,
            @NotBlank @RequestParam(required = false) String seq,
            @NotBlank @RequestParam Integer limit,
            @NotBlank @RequestParam(required = false) boolean improve
    ) {
        // TODO 視情況而從不同的來源獲取 list (todoService, historyListService)
        if (improve) {
            return ResponseResult.successGet(historyListService.getList(startTime, seq, limit));
        }

        return ResponseResult.successGet(todoService.getList(startTime, seq, limit));
    }

}
