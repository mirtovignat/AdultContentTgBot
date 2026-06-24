package com.example.demo.service;

import com.example.demo.dto.BonusResult;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BonusService {

    private final UserRepository userRepository;

    @Value("${bonus.cooldown-seconds:10800}")
    private long cooldownSeconds;

    public BonusResult claimBonus(Long chatId) {
        User user = userRepository.findByChatIdOrThrow(chatId);
        LocalDateTime now = LocalDateTime.now();

        if (user.getLastBonusAt() != null) {
            long secondsPassed = Duration.between(user.getLastBonusAt(), now).getSeconds();
            if (secondsPassed < cooldownSeconds) {
                long left = cooldownSeconds - secondsPassed;
                return BonusResult.blocked(left);
            }
        }

        user.setBonuses(user.getBonuses() + 1);
        user.setLastBonusAt(now);
        userRepository.save(user);

        return BonusResult.success();
    }

    public long getLeftSeconds(Long chatId) {
        User user = userRepository.findByChatIdOrThrow(chatId);
        if (user.getLastBonusAt() == null) {
            return 0;
        }
        long secondsPassed = Duration.between(user.getLastBonusAt(), LocalDateTime.now()).getSeconds();
        return Math.max(0, cooldownSeconds - secondsPassed);
    }
}