package com.example.todolist.protocol.http.response;

import com.alibaba.fastjson.JSON;
import com.example.todolist.common.ResponseCode;

public class Response {
    private int code;
    private String message;
    private Object data;

    public Response(){
    }

    public Response(ResponseCode responseCode) {
        this.code = responseCode.getCode();
        this.message=responseCode.getMessage();
    }

    public Response(ResponseCode responseCode,Object data) {
        this.code = responseCode.getCode();
        this.message=responseCode.getMessage();
        this.data=data;
    }




    public Response setCode(ResponseCode responseCode) {
        this.code = responseCode.getCode();
        this.message = responseCode.getMessage();
        return this;
    }

    public int getCode() {
        return code;
    }

    public Response setCode(int code) {
        this.code = code;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public Response setMessage(String message) {
        this.message = message;
        return this;
    }

    public Object getData() {
        return data;
    }

    public Response setData(Object data) {
        this.data = data;
        return this;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
