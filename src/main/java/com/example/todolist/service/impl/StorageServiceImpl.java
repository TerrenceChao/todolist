package com.example.todolist.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.todolist.mq.rabbit.producer.RabbitProducer;
import com.example.todolist.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class StorageServiceImpl implements StorageService {

    @Autowired
    private RabbitProducer rabbitProducer;

    @Override
    public void uploadAttach(Long tid, JSONObject attachments, List<MultipartFile> files) {
        log.info("tid:{}, \nattachments:{}, \nfiles:{} \nfile amount:{}", tid, attachments, files, files.size());
        files.forEach(file -> {
            JSONObject message = toQueueMessage(tid, attachments, file);
            if (! message.isEmpty()) {
                rabbitProducer.sendMessage("cloud-storage-worker", message);
            }
        });
    }

    private JSONObject toQueueMessage(Long tid, JSONObject attachments, MultipartFile file) {
        JSONObject fileMeta = attachments.getJSONObject(file.getOriginalFilename());
        JSONObject message = new JSONObject();

        try {
            message.put("tid", tid);
            message.put("hash", fileMeta.getString("hash"));
            message.put("contentType", file.getContentType());
            message.put("filename", file.getOriginalFilename());

            log.info("file's message tid:{} \nmessage:{}", tid, message);

            message.put("bytes", file.getBytes());
        } catch (IOException e) {
            log.error("file bytes error", e.getMessage());
        }

        return message;
    }
}
