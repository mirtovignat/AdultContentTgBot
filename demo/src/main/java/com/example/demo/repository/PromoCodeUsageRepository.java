package com.example.demo.repository;

import com.example.demo.entity.PromoCode;
import com.example.demo.entity.PromoCodeUsage;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PromoCodeUsageRepository extends JpaRepository<PromoCodeUsage, Long> {
    boolean existsByPromoCodeAndUser(PromoCode promoCode, User user);
    long countByPromoCode(PromoCode promoCode);
}