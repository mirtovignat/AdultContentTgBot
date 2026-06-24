package com.example.demo.service;

import com.example.demo.entity.PromoCode;
import com.example.demo.entity.PromoCodeUsage;
import com.example.demo.entity.User;
import com.example.demo.exception.*;
import com.example.demo.repository.PromoCodeRepository;
import com.example.demo.repository.PromoCodeUsageRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PromoCodeService {

    private final PromoCodeRepository promoCodeRepository;
    private final PromoCodeUsageRepository promoCodeUsageRepository;
    private final UserRepository userRepository;

    @Transactional
    public PromoCode createPromoCode(String code, Long bonusAmount, LocalDateTime expiresAt, Long usageLimit) {
        if (promoCodeRepository.existsByCode(code)) {
            throw new PromoCodeDuplicateException();
        }
        PromoCode promoCode = new PromoCode();
        promoCode.setCode(code);
        promoCode.setBonusAmount(bonusAmount);
        promoCode.setExpiresAt(expiresAt);
        promoCode.setUsageLimit(usageLimit);
        promoCode.setUsedCount(0L);
        promoCode.setIsActive(true);
        return promoCodeRepository.save(promoCode);
    }

    @Transactional
    public void deletePromoCode(Long id) {
        promoCodeRepository.deleteById(id);
    }

    public List<PromoCode> getAllActive() {
        return promoCodeRepository.findAll().stream()
                .filter(PromoCode::getIsActive)
                .toList();
    }

    public PromoCode findByCode(String code) {
        return promoCodeRepository.findByCode(code)
                .orElseThrow(PromoCodeNotFoundException::new);
    }

    @Transactional
    public void applyPromoCode(String code, Long chatId) {
        PromoCode promoCode = findByCode(code);

        if (!promoCode.getIsActive()) {
            throw new PromoCodeInactiveException();
        }

        if (promoCode.getExpiresAt() != null && promoCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new PromoCodeExpiredException();
        }

        if (promoCode.getUsageLimit() != null &&
                promoCode.getUsedCount() >= promoCode.getUsageLimit()) {
            throw new PromoCodeLimitReachedException();
        }

        User user = userRepository.findByChatIdOrThrow(chatId);

        if (promoCodeUsageRepository.existsByPromoCodeAndUser(promoCode, user)) {
            throw new PromoCodeAlreadyUsedException();
        }

        user.setBonuses(user.getBonuses() + promoCode.getBonusAmount());
        userRepository.save(user);

        PromoCodeUsage usage = new PromoCodeUsage();
        usage.setPromoCode(promoCode);
        usage.setUser(user);
        promoCodeUsageRepository.save(usage);

        promoCode.setUsedCount(promoCode.getUsedCount() + 1);
        promoCodeRepository.save(promoCode);
    }
}