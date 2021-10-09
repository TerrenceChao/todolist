package com.example.todolist.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.todolist.mq.rabbit.producer.RabbitProducer;
import com.example.todolist.service.AttachService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class AttachServiceImpl implements AttachService {

    @Autowired
    private RabbitProducer rabbitProducer;

    @Override
    public boolean hasAttach(JSONObject payload) {
        // TODO 從 google cloud storage 查詢是否有既存的 檔案
//        Long tid = payload.getLong("tid");
//        Integer partitionKey = payload.getInteger("partitionKey");
//        String filename = payload.getString("filename");
//        String hash = payload.getString("hash");
//        ...

        return true;
    }

    /**
     * @param tid
     * @param partitionKey weekOfYear
     * @param attachments
     * @param files
     */
    @Override
    public void uploadAttach(Long tid, Integer partitionKey, JSONObject attachments, List<MultipartFile> files) {
        log.info("tid:{}, \nattachments:{}, \nfiles:{} \nfile amount:{}", tid, attachments, files, files.size());
        files.forEach(file -> {
            JSONObject message = toQueueMessage(tid, partitionKey, attachments, file);
            if (! message.isEmpty()) {
                rabbitProducer.sendMessage("cloud-storage-worker", message);
            }
        });
    }

    private JSONObject toQueueMessage(Long tid, Integer partitionKey, JSONObject attachments, MultipartFile file) {
        JSONObject fileMeta = attachments.getJSONObject(file.getOriginalFilename());
        JSONObject message = new JSONObject();

        try {
            message.put("tid", tid);
            message.put("partitionKey", partitionKey);
            message.put("hash", fileMeta.getString("hash"));
            message.put("contentType", file.getContentType());
            message.put("filename", file.getOriginalFilename());

            log.info("file's message tid:{} \nmessage:{}", tid, message);

            message.put("bytes", file.getBytes());
        } catch (IOException e) {
            log.error("file bytes error ", e.getMessage());
        }

        return message;
    }
}
