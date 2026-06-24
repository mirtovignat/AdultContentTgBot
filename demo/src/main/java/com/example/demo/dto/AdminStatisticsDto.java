package com.example.demo.dto;

public record AdminStatisticsDto(
        long totalUsers,
        long totalBonuses,
        long totalPromoCodes,
        long usedPromoCodes,
        long activePromoCodes
) {}