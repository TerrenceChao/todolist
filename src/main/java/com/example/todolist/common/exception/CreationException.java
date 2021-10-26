package com.example.todolist.common.exception;

import com.example.todolist.common.ResponseCode;

public class CreationException extends BaseException {

    public CreationException(ResponseCode responseCode) {
        super(responseCode);
    }
}
