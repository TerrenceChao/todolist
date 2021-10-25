package com.example.todolist.common.exception;

import com.example.todolist.common.ResponseCode;
import lombok.Data;


@Data
public class BaseException extends RuntimeException {

    protected int code;
    protected String message;
    protected Object data;

    public BaseException(ResponseCode responseCode) {
        this.code = responseCode.getCode();
        this.message = responseCode.getMessage();
    }

    public BaseException(ResponseCode responseCode, Object data) {
        this.code = responseCode.getCode();
        this.message = responseCode.getMessage();
        this.data = data;
    }

    public BaseException(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public BaseException(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
}
