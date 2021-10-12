package com.example.todolist.db.rmdb.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
@TableName(value = "attachment")
public class Attachment {

    /** aid = hashcode */
    @TableId
    private String aid;

    private Long createdAt;
}
