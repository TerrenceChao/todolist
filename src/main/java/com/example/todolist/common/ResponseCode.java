package com.example.todolist.common;

public enum ResponseCode {

    FAIL_FORMATTER(501, "{}"),
    SUCCESS(1, "登入成功"),
    FAIL(400, "無法訪問"),
    SYSTEM_ERROR(409, "系統異常"),
    TOEKNUNVALIBLE(405, "TOKEN過期"),
    NOT_FOUND(404, "此位置不存在"),
    ;

    private int code;
    private String message;

    ResponseCode(int code, String message) {
        this.code = code;
        this.message = message;
    }


    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
