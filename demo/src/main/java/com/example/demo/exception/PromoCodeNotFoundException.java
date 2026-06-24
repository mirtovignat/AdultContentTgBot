package com.example.demo.exception;

public class PromoCodeNotFoundException extends AbstractException {
  public PromoCodeNotFoundException() {
    super(ErrorCode.PROMO_CODE_NOT_FOUND, "Промокод не найден");
  }
}