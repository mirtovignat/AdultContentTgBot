package com.example.demo.dto;

public record BonusResult(
        boolean allowed,
        long secondsLeft
) {
    public static BonusResult success() {
        return new BonusResult(true, 0);
    }

    public static BonusResult blocked(long secondsLeft) {
        return new BonusResult(false, secondsLeft);
    }
}