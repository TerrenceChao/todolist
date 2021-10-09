package com.example.todolist.service;

import com.alibaba.fastjson.JSONObject;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AttachService {

    boolean hasAttach(JSONObject payload);
    void uploadAttach(Long tid, Integer partitionKey, JSONObject attachments, List<MultipartFile> files);
}
