package com.example.demo.exception;

public class PromoCodeAlreadyUsedException extends AbstractException {
  public PromoCodeAlreadyUsedException() {
    super(ErrorCode.PROMO_CODE_ALREADY_USED, "Вы уже использовали этот промокод");
  }
}