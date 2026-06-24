package com.example.demo.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserNotFoundException extends AbstractException {
    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND, "Пользователь не найден!");
    }
}