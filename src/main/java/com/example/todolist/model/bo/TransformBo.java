package com.example.todolist.model.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

@Data
@Accessors(chain = true)
public class TransformBo {

    @NotBlank
    private String seq;

    @NotBlank
    private Integer limit;
}
