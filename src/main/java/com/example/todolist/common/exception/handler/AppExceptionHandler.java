package com.example.todolist.common.exception.handler;


import com.example.todolist.common.exception.*;
import com.example.todolist.protocol.http.response.Response;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;


@RestControllerAdvice
public class AppExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {ReqFormatException.class})
    public ResponseEntity<Object> reqFormatException(BaseException ex, WebRequest request) {
        return exception(ex, request, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {ReqNotFoundException.class})
    public ResponseEntity<Object> reqNotFoundException(BaseException ex, WebRequest request) {
        return exception(ex, request, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = {CreationException.class})
    public ResponseEntity<Object> creationException(BaseException ex, WebRequest request) {
        return exception(ex, request, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = {SearchException.class})
    public ResponseEntity<Object> searchException(BaseException ex, WebRequest request) {
        return exception(ex, request, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    protected ResponseEntity<Object> exception(BaseException ex, WebRequest request, HttpStatus httpStatus) {
        Response errorResponse = new Response(ex.getCode(), ex.getMessage());

        return new ResponseEntity<>(
                errorResponse,
                new HttpHeaders(),
                httpStatus
        );
    }
}
