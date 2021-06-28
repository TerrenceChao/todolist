package com.example.todolist.db.rmdb.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TableName(value = "t_user") // 因為不是系統預設以為的 "user", 所以這邊需要指定名稱
public class User {

    private Long userId;
    private String username;
    private String ustatus;
}
