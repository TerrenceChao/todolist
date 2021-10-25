package com.example.todolist.common.exception;

import com.example.todolist.common.ResponseCode;

public class ReqFormatException extends BaseException {

    public ReqFormatException(String message) {
        super(
                ResponseCode.FORMATTER_ERROR.getCode(),
                message
        );
    }
}
