package com.example.todolist.common;

public enum ResponseCode {

    SUCCESS(1, "success"),

    FORMATTER_ERROR(40000, "format error"),
    TASK_NOT_FOUND(40401, "todo-task is not found."),
    TODOLIST_NOT_FOUND(40402, "todo-list is not found. "),

    TASK_CREATION_ERROR(50000, "todo-task creation error"),
    TASK_SEARCH_ERROR(50001, "todo-task search error"),
    TODOLIST_CREATION_ERROR(50010, "todo-list creation error"),
    TODOLIST_SEARCH_ERROR(50011, "todo-list search error"),

    FAIL(99999, "request fail"),
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
