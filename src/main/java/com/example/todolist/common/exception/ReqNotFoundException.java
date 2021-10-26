package com.example.todolist.common.exception;

import com.example.todolist.common.ResponseCode;

public class ReqNotFoundException extends BaseException {

    public ReqNotFoundException(ResponseCode responseCode, String message) {
        super(
                responseCode.getCode(),
                responseCode.getMessage() + message
        );
    }
}
