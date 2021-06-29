package com.example.todolist.protocol.http.response;

import com.example.todolist.common.ResponseCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseResult {
    private static final String DEFAULT_SUCCESS_MESSAGE = "SUCCESS";
    private static final String DEFAULT_FAILED_MESSAGE = "FAILED";

    public static ResponseEntity success(Object body, int rawStatus) {
        return new ResponseEntity(success(body), null, rawStatus);
    }

    public static ResponseEntity success(Object body, HttpStatus status) {
        return new ResponseEntity(success(body), null, status);
    }

    public static ResponseEntity successGet(Object body) {
        return new ResponseEntity(success(body), null, HttpStatus.OK);
    }

    public static ResponseEntity successPost(Object body) {
        return new ResponseEntity(success(body), null, HttpStatus.CREATED);
    }

    public static Response success() {
        return new Response()
                .setCode(ResponseCode.SUCCESS)
                .setMessage(DEFAULT_SUCCESS_MESSAGE);
    }

    public static Response success(Object data) {
        return new Response()
                .setCode(ResponseCode.SUCCESS)
                .setMessage(DEFAULT_SUCCESS_MESSAGE)
                .setData(data);
    }

    public static Response success(String message, Object data) {
        return new Response()
                .setCode(ResponseCode.SUCCESS)
                .setMessage(message)
                .setData(data);
    }

    public static Response fail(String message) {
        return new Response()
                .setCode(ResponseCode.FAIL)
                .setMessage(message);
    }

    public static Response fail(int code, String message) {
        return new Response()
                .setCode(code)
                .setMessage(message);
    }

    public static Response genError() {
        return new Response()
                .setCode(ResponseCode.FAIL)
                .setMessage(DEFAULT_FAILED_MESSAGE);
    }
}
