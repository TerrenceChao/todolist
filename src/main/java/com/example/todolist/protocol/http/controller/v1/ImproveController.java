package com.example.todolist.protocol.http.controller.v1;

import com.example.todolist.model.bo.TransformBo;
import com.example.todolist.mq.rabbit.producer.RabbitProducer;
import com.example.todolist.protocol.http.response.ResponseResult;
import com.example.todolist.service.HistoryListService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/improve")
public class ImproveController {

    @Autowired
    private HistoryListService historyListService;

    @PostMapping(value = "/transform", consumes = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity transform(@RequestBody TransformBo transformBo) {
return null; // TODO ...
    }

    @Autowired
    private RabbitProducer rabbitProducer;

    @GetMapping(value = "/mq", consumes = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity mq(@RequestBody TransformBo transformBo) {
//        rabbitTemplate.convertAndSend("cloud-storage-worker", "", transformBo.getSeq() + "-" + transformBo.getLimit());
        for (int i = 1; i <= 21; i++) {
            rabbitProducer.sendMessage(
                    "",
                    "cloud-storage-worker",
                    i + " " + transformBo.getSeq() + "-" + transformBo.getLimit()
            );
        }
        return ResponseResult.successGet(transformBo); // TODO ...
    }
}
