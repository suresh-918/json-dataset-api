package com.freightfox.jsondataset.exception;

import com.freightfox.jsondataset.dto.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<CommonResponse<Void>> handleInvalidRequest(
            InvalidRequestException exception) {

        return ResponseEntity.badRequest().body(
                new CommonResponse<>(
                        HttpStatus.BAD_REQUEST.value(),
                        exception.getMessage(),
                        null,
                        false
                )
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<CommonResponse<Void>> handleInvalidJson(
            HttpMessageNotReadableException exception) {

        return ResponseEntity.badRequest().body(
                new CommonResponse<>(
                        HttpStatus.BAD_REQUEST.value(),
                        "Request body contains invalid JSON",
                        null,
                        false
                )
        );
    }
}
