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
    public void deletePromoCode(Long id) {
        promoCodeRepository.deleteById(id);
    }

    public List<PromoCode> getAllActive() {
        return promoCodeRepository.findAll().stream()
                .filter(PromoCode::getIsActive)
                .toList();
    }

    @Transactional
    public PromoCode createPromoCode(
            String code,
            Long bonusAmount,
            LocalDateTime expiresAt,
            Long usageLimit
    ) {
        if (promoCodeRepository.existsByCode(code)) {
            throw new BusinessException(ErrorCode.PROMO_CODE_DUPLICATE);
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

    public PromoCode findByCode(String code) {
        return promoCodeRepository.findByCode(code)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.PROMO_CODE_NOT_FOUND));
    }

    @Transactional
    public void applyPromoCode(String code, Long chatId) {
        PromoCode promoCode = findByCode(code);

        if (!promoCode.getIsActive()) {
            throw new BusinessException(ErrorCode.PROMO_CODE_INACTIVE);
        }

        if (promoCode.getExpiresAt() != null
                && promoCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.PROMO_CODE_EXPIRED);
        }

        if (promoCode.getUsageLimit() != null
                && promoCode.getUsedCount() >= promoCode.getUsageLimit()) {
            throw new BusinessException(ErrorCode.PROMO_CODE_LIMIT_REACHED);
        }

        User user = userRepository.findByChatIdOrThrow(chatId);

        if (promoCodeUsageRepository.existsByPromoCodeAndUser(promoCode, user)) {
            throw new BusinessException(ErrorCode.PROMO_CODE_ALREADY_USED);
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

    @Transactional
    public void activatePromoCode(
            Long chatId,
            String code
    ) {

        PromoCode promoCode = findByCode(code);

        validatePromoCode(
                promoCode,
                chatId
        );

        applyPromoCode(
                promoCode,
                chatId
        );
    }

    private void validatePromoCode(
            PromoCode promoCode,
            Long chatId
    ) {

        if (!promoCode.getIsActive()) {
            throw new BusinessException(
                    ErrorCode.PROMO_CODE_INACTIVE
            );
        }

        if (
                promoCode.getExpiresAt() != null
                        && promoCode.getExpiresAt()
                        .isBefore(LocalDateTime.now())
        ) {
            throw new BusinessException(
                    ErrorCode.PROMO_CODE_EXPIRED
            );
        }

        if (
                promoCode.getUsageLimit() != null
                        && promoCode.getUsedCount()
                        >= promoCode.getUsageLimit()
        ) {
            throw new BusinessException(
                    ErrorCode.PROMO_CODE_LIMIT_REACHED
            );
        }

        User user =
                userRepository.findByChatIdOrThrow(chatId);

        if (
                promoCodeUsageRepository.existsByPromoCodeAndUser(
                        promoCode,
                        user
                )
        ) {
            throw new BusinessException(
                    ErrorCode.PROMO_CODE_ALREADY_USED
            );
        }
    }

    private void applyPromoCode(
            PromoCode promoCode,
            Long chatId
    ) {

        User user =
                userRepository.findByChatIdOrThrow(chatId);

        user.setBonuses(
                user.getBonuses()
                        + promoCode.getBonusAmount()
        );

        userRepository.save(user);

        PromoCodeUsage usage = new PromoCodeUsage();

        usage.setPromoCode(promoCode);
        usage.setUser(user);

        promoCodeUsageRepository.save(usage);

        promoCode.setUsedCount(
                promoCode.getUsedCount() + 1
        );

        promoCodeRepository.save(promoCode);
    }
}