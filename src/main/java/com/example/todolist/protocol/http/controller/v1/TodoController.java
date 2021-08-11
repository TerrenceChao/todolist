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
     *
     * @param title
     * @param content
     * @param files
     * @return
     * @throws IOException
     */
    @PostMapping(value = "/tasks", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity create(@RequestPart("title") String title, @RequestPart("content") String content, @RequestPart("files") List<MultipartFile> files) throws IOException {
        TodoTaskVo taskVo = todoService.create(title, content, files);
        // async, threadlocal > historyListService. ...

        return ResponseResult.successPost(taskVo.getTid());
    }
    
    
    /**
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
            @NotBlank @RequestParam Integer limit
    ) {

        return ResponseResult.successGet(todoService.getList(startTime, seq, limit));
    }

}
