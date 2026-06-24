package com.example.demo.exception;

public class PromoCodeDuplicateException extends AbstractException {
  public PromoCodeDuplicateException() {
    super(ErrorCode.PROMO_CODE_DUPLICATE, "Промокод с таким кодом уже существует");
  }
}