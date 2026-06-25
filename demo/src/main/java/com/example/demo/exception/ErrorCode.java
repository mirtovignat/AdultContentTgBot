package com.example.demo.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    USER_ALREADY_REGISTERED("Пользователь уже зарегистрирован!"),
    USER_NOT_FOUND("Пользователь не найден!"),

    PROMO_CODE_NOT_FOUND("Промокод не найден"),
    PROMO_CODE_EXPIRED("Срок действия промокода истёк"),
    PROMO_CODE_ALREADY_USED("Вы уже использовали этот промокод"),
    PROMO_CODE_LIMIT_REACHED("Лимит использований промокода исчерпан"),
    PROMO_CODE_INACTIVE("Промокод деактивирован"),
    PROMO_CODE_DUPLICATE("Промокод с таким кодом уже существует");

    private final String message;
}