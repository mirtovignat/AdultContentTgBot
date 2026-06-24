package com.example.demo.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserAlreadyRegisteredException extends AbstractException {
    public UserAlreadyRegisteredException() {
        super(ErrorCode.USER_ALREADY_REGISTERED, "Пользователь уже зарегистрирован!");
    }
}
