package com.example.demo.exception;

public class PromoCodeLimitReachedException extends AbstractException {
  public PromoCodeLimitReachedException() {
    super(ErrorCode.PROMO_CODE_LIMIT_REACHED, "Лимит использований промокода исчерпан");
  }
}