package com.example.demo.service;

import com.example.demo.dto.StarsOffer;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "stars")
public class StarsProperties {
    private List<StarsOffer> offers;
}