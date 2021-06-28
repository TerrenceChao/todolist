package com.example.todolist.db.rmdb.entity;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Course {

    private Long cid;
    private String cname;
    private Long userId;
    private String cstatus;
}
