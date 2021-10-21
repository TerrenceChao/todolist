package com.example.todolist.protocol.http.controller.v2;

import com.example.todolist.model.bo.TransformBo;
import com.example.todolist.model.vo.BatchVo;
import com.example.todolist.model.vo.TodoTaskVo;
import com.example.todolist.protocol.http.response.ResponseResult;
import com.example.todolist.service.HistoryListService;
import com.example.todolist.service.TriggerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RequiredArgsConstructor
@RequestMapping("/v2/history")
@RestController("v2HistoryController")
public class HistoryController {

    @Autowired
    private HistoryListService historyListService;

    @Autowired
    @Qualifier("triggerServiceB")
    private TriggerService triggerService;

    @PostMapping(value = "/transform", consumes = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity transform(@RequestBody TransformBo transformBo) {
        BatchVo vo = historyListService.transform(transformBo.getSeq(), transformBo.getLimit());
        return ResponseResult.successPost(vo);
    }

    /**
     * TODO 補償機制 由外部 scheduler 觸發 API
     * @param limit
     * @return
     */
    @PostMapping(value = "/transform/{limit}", consumes = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity transformLimit(@PathVariable Integer limit) {
        BatchVo vo = historyListService.transform(limit);
        if (! vo.getList().isEmpty()) {
            TodoTaskVo firstOne = (TodoTaskVo) vo.getList().get(0);
            triggerService.setLastTimestamp(firstOne.getCreatedAt().getTime());
        }

        return ResponseResult.successPost(vo);
    }
}
