package com.example.todolist.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Collections;
import java.util.List;

@Data
@AllArgsConstructor
@Accessors(chain = true)
public class BatchVo<T> extends BaseVo {

    private List<T> list;

    /** request limit */
    private Integer limit;

    /** response batch */
    private Integer batch;

    private Object next;

    /**
     * constructor content: empty
     * @param limit
     */
    public BatchVo(Integer limit) {
        this.list = Collections.EMPTY_LIST;
        this.limit = limit;
        this.batch = 0;
    }
}
