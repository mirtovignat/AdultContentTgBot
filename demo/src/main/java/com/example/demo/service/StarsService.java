package com.example.demo.service;

import com.example.demo.dto.StarsOffer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StarsService {

    private final StarsProperties starsProperties;

    public List<StarsOffer> getOffers() {
        return starsProperties.getOffers();
    }

    public int getBonusForStars(int stars) {
        return starsProperties.getOffers().stream()
                .filter(o -> o.stars() == stars)
                .map(StarsOffer::bonus)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Неверное количество Stars"));
    }
}