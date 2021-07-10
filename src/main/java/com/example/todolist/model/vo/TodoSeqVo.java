package com.example.todolist.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TodoSeqVo extends BaseVo {

    private Integer month;

    private Integer weekOfYear;

    private Long lid;
}
