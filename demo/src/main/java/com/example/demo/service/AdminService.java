package com.example.demo.service;


import com.example.demo.dto.AdminStatisticsDto;
import com.example.demo.repository.PromoCodeRepository;
import com.example.demo.repository.PromoCodeUsageRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final PromoCodeRepository promoCodeRepository;
    private final PromoCodeUsageRepository promoCodeUsageRepository;

    public AdminStatisticsDto getStatistics() {
        long totalUsers = userRepository.count();
        long totalBonuses = userRepository.findAll().stream()
                .mapToLong(u -> u.getBonuses() != null ? u.getBonuses() : 0L)
                .sum();
        long totalPromoCodes = promoCodeRepository.count();
        long usedPromoCodes = promoCodeUsageRepository.count();
        long activePromoCodes = promoCodeRepository.findAll().stream()
                .filter(p -> p.getIsActive() != null && p.getIsActive())
                .count();
        return new AdminStatisticsDto(totalUsers, totalBonuses, totalPromoCodes, usedPromoCodes, activePromoCodes);
    }

    public void addBonuses(Long chatId, Long amount) {
        var user = userRepository.findByChatIdOrThrow(chatId);
        user.setBonuses(user.getBonuses() + amount);
        userRepository.save(user);
    }
}