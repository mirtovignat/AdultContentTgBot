package com.example.demo.exception;

public class PromoCodeExpiredException extends AbstractException {
  public PromoCodeExpiredException() {
    super(ErrorCode.PROMO_CODE_EXPIRED, "Срок действия промокода истёк");
  }
}