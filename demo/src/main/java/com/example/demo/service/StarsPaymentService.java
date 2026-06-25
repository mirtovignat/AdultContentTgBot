package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.payments.SuccessfulPayment;

import java.lang.reflect.Field;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StarsPaymentService {

    private final UserRepository userRepository;

    private static final String PAYLOAD_PREFIX = "bonus_";

    public String generatePayload(int bonusAmount) {
        return PAYLOAD_PREFIX + bonusAmount + "_" + UUID.randomUUID();
    }

    public String extractPayload(SuccessfulPayment payment) {
        try {
            Field field = payment.getClass().getDeclaredField("payload");
            field.setAccessible(true);
            return (String) field.get(payment);
        } catch (Exception e) {
            throw new RuntimeException("Не удалось получить payload", e);
        }
    }

    public int extractBonusFromPayload(String payload) {
        if (payload == null || !payload.startsWith(PAYLOAD_PREFIX)) {
            throw new IllegalArgumentException("Неверный payload");
        }

        String[] parts = payload.split("_");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Неверный payload формат");
        }

        return Integer.parseInt(parts[1]);
    }

    public User addBonuses(Long chatId, int bonusAmount) {
        User user = userRepository.findByChatIdOrThrow(chatId);
        user.setBonuses(user.getBonuses() + bonusAmount);
        return userRepository.save(user);
    }
}