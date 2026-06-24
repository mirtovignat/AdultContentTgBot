package com.example.demo.exception;

public class PromoCodeInactiveException extends AbstractException {
  public PromoCodeInactiveException() {
    super(ErrorCode.PROMO_CODE_INACTIVE, "Промокод деактивирован");
  }
}