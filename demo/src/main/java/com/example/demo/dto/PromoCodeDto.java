package com.example.demo.dto;

import java.time.LocalDateTime;

public record PromoCodeDto(
        Long id,
        String code,
        Long bonusAmount,
        LocalDateTime expiresAt,
        Long usageLimit,
        Long usedCount,
        Boolean isActive,
        LocalDateTime createdAt
) {
    public String toPrettyString() {
        return String.format(
                "🎫 Код: %s\n" +
                        "💰 Бонус: %d\n" +
                        "⏳ Срок: %s\n" +
                        "🔢 Лимит: %s\n" +
                        "📊 Использован: %d раз\n" +
                        "✅ Активен: %s",
                code,
                bonusAmount,
                expiresAt == null ? "Бессрочный" : expiresAt.toString(),
                usageLimit == null ? "Безлимитный" : usageLimit.toString(),
                usedCount,
                isActive ? "Да" : "Нет"
        );
    }
}