package com.example.todolist.protocol.http.controller.v1;

import com.example.todolist.model.bo.TodoTaskBo;
import com.example.todolist.model.vo.BatchVo;
import com.example.todolist.model.vo.TodoTaskVo;
import com.example.todolist.protocol.http.response.ResponseResult;
import com.example.todolist.service.TodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

import java.time.ZonedDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/todo")
public class TodoController {

    @Autowired
    private TodoService todoService;

    /**
     * TODO How WebFlux works?
     *
     * TODO 每 "K" 筆(寫入流量很大怎辦???, 用 MQ 異步解決) 或是 30 secs(寫入過慢) 寫入 DB: todo_list;  >>> !?!??!?
     *  不到 "K" 筆在 redis 需有一份 copy (最新的 < "K" 筆)  >>> !?!??!?
     *
     * 1. create one in DB
     * 2. if the amount of latest tasks in cache(redis) < "K" ?  >>> !?!??!?
     *      Y: read one from DB and write cache (有辦法透過 snowflake 拿到 cid ?? Y)
     *      N:  1) mark/label for these "K" tasks.
     *          2) write into todo_list; using RabbitMQ
     *          3) clear cache from ??? to latest cid if 1) is done.
     */
    @PostMapping(value = "/task")
    public ResponseEntity<Long> create(@RequestBody TodoTaskBo todoTaskBo) {
        // step 1
        Long tid = todoService.create(todoTaskBo);

        // TODO  step 2  1).. 2).. 3)  >> async async async
        // transformService. ...

        return ResponseResult.successPost(tid);
    }
    
    
    /**
     * TODO 單庫透過 week_of_year 做分表, 所以除了 tid(PK) 以外，再透過 week_of_year 針對特定 partition 查詢，
     *  如此只會查詢單庫單表
     * @param tid
     * @param weekOfYear
     * @return
     */
    @GetMapping(value = "/task/{tid}", produces = "application/json;charset=utf-8")
    public ResponseEntity<TodoTaskVo> getByTid(@PathVariable Long tid, @NotBlank @RequestParam Integer weekOfYear) {
        return ResponseResult.successGet(todoService.getOne(tid, weekOfYear));
    }

    /**
     * TODO 讀取 TodoList (1筆 TodoList 包含 10 筆 TodoTask)
     * TODO 最新的 (< "K" 筆) 會在 redis 裡面!?!??!?
     * @param startTime
     * @param seq (not required) if null, get the min seq of the time 
     * @param batch
     * @return
     */
    @GetMapping(value = "/list", produces = "application/json;charset=utf-8")
    public ResponseEntity<BatchVo> getList(
            @NotBlank @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime startTime,
            @NotBlank @RequestParam(required = false) String seq,
            @NotBlank @RequestParam Integer batch) {
        return ResponseResult.successGet(todoService.getList(startTime, seq, batch));
    }
}
