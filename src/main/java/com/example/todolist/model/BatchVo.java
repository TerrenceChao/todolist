package com.example.todolist.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@AllArgsConstructor
@Accessors(chain = true)
public class BatchVo<T> {

    private List<T> list;

    /** request limit */
    private Long limit;

    /** response batch */
    private Long batch;

    private Object next;
}
