package com.example.todolist.db.rmdb.repo;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.todolist.db.rmdb.entity.Attachment;
import com.example.todolist.db.rmdb.mapper.AttachmentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public class AttachmentRepository {

    @Autowired
    private AttachmentMapper attachmentMapper;

    public Attachment findById(String id) {
        QueryWrapper<Attachment> wrapper = new QueryWrapper<Attachment>()
                .eq("aid", id);

        return attachmentMapper.selectOne(wrapper);
    }

    public Attachment insert(String id, Long createdAt) {
        Attachment attachment = new Attachment()
                .setAid(id)
                .setCreatedAt(createdAt);
        attachmentMapper.insert(attachment);

        return attachment;
    }
}
