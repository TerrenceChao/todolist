package com.example.todolist.model.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

@Data
@Accessors(chain = true)
public class TodoTaskBo {

    private Long tid;

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    /**
     * JSON
     * [
     *      { name, hash, file? },
     *      { name, hash, file? },
     *      ...
     * ]
     */
    private String attachments;
}
