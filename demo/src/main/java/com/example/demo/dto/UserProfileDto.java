package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserProfileDto {

    private String username;
    private String firstName;
    private String lastName;

    private LocalDateTime registeredAt;
    private LocalDateTime lastBonusAt;

    private Long bonuses;
    private Long referralsCount;
    private Long referredBy;

    private int filesCount;

    @Override
    public String toString() {
        return "👤 Профиль\n\n" +
                "👤 Username: @" + getUsername() + "\n" +
                "📛 Имя: " + getFirstName() + "\n" +
                "📛 Фамилия: " + getLastName() + "\n\n" +
                "🎁 Баланс: " + getBonuses() + "\n" +
                "👥 Рефералы: " + getReferralsCount() + "\n" +
                "🔗 Пригласил: " + (getReferredBy() == null ?
                "Вы не были приглашены" : "Пользователь #" + getReferredBy()) +
                "📦 Файлов: " + getFilesCount() + "\n\n" +
                "📅 Регистрация: " +
                (registeredAt == null ? "—" :
                        registeredAt.format(
                                java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
                        ));
    }
}