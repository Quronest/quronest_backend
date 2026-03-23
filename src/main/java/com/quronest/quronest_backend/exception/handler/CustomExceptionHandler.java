package com.quronest.quronest_backend.exception.handler;

import com.quronest.quronest_backend.dto.ApiErrorDto;
import com.quronest.quronest_backend.exception.CustomApiErrorResponseException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ApiErrorDto> handle(MethodArgumentNotValidException exception) {
        List<String> errors = exception
                .getBindingResult()
                .getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();

        ApiErrorDto apiError = new ApiErrorDto("validation", errors);
        return new ResponseEntity<ApiErrorDto>(apiError, (HttpHeaders) null, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ApiErrorDto> handle(CustomApiErrorResponseException exception) {
        List<String> errors = new ArrayList<>();
        errors.add(exception.getError());

        ApiErrorDto apiError = new ApiErrorDto(exception.getErrorType(), errors);
        return new ResponseEntity<ApiErrorDto>(apiError, (HttpHeaders) null, exception.getStatus());
    }

    @ExceptionHandler
    public ResponseEntity<ApiErrorDto> handle(MethodArgumentTypeMismatchException exception) {
        String errorMessage = String.format("Invalid value for parameter '%s'", exception.getName());

        List<String> errors = List.of(errorMessage);

        ApiErrorDto apiError = new ApiErrorDto("validation", errors);
        return new ResponseEntity<ApiErrorDto>(apiError, (HttpHeaders) null, HttpStatus.BAD_REQUEST);
    }
}
