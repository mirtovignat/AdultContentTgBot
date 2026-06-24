package com.example.demo.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AbstractException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String message;

    public AbstractException(ErrorCode errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

}
