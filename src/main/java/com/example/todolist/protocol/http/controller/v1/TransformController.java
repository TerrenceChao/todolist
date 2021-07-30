package com.example.todolist.protocol.http.controller.v1;

import com.example.todolist.model.bo.TransformBo;
import com.example.todolist.model.vo.BatchVo;
import com.example.todolist.protocol.http.response.ResponseResult;
import com.example.todolist.service.HistoryListService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/transform")
public class TransformController {

    @Autowired
    private HistoryListService historyListService;

    @PostMapping(value = "/transform", consumes = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity transform(@RequestBody TransformBo transformBo) {
        BatchVo vo = historyListService.transform(transformBo.getSeq(), transformBo.getLimit());
        return ResponseResult.successPost(vo);
    }

}
