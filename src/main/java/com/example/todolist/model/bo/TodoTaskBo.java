package com.example.todolist.model.bo;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TodoTaskBo extends BaseBo {

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    /**
     * JSON
     * {
     *      files: [
     *          { name, hash, url },
     *          { name, hash, url },
     *          ...
     *      ]
     * }
     */
    @JsonIgnore
    private JSONObject attachments;
}
