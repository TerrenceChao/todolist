package com.example.todolist.db.rmdb.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TableName(value = "attachment")
public class Attachment {

    @TableId
    private String hashcode;
    private String url;
}
