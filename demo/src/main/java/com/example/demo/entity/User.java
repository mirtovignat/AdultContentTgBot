package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.telegram.telegrambots.meta.api.methods.CopyMessage;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "users")
public class User extends AbstractEntity {
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role = Role.USER;

    @Column(name = "chat_id", nullable = false, unique = true)
    private Long chatId;

    @Column(name = "telegram_id", unique = true)
    private Long telegramId;

    @Column(name = "username")
    private String username;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @CreationTimestamp
    @Column(name = "registered_at", nullable = false, updatable = false)
    private LocalDateTime registeredAt;

    @Column(name = "last_bonus_at")
    private LocalDateTime lastBonusAt;

    @Column(name = "bonuses", nullable = false)
    private Long bonuses;

    @Column(name = "referrals_count", nullable = false)
    private Long referralsCount;

    @Column(name = "referred_by")
    private Long referredBy;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<PornFile> files = new LinkedHashSet<>();

}