package com.example.todolist.service;

import com.alibaba.fastjson.JSONObject;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface StorageService {

    void uploadAttach(Long tid, JSONObject attachments, List<MultipartFile> files);
}
