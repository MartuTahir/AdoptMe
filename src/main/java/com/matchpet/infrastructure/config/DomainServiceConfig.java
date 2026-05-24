package com.matchpet.infrastructure.config;

import com.matchpet.domain.service.CompatibilityEngine;
import com.matchpet.domain.service.ImpulsivityEngine;
import com.matchpet.domain.service.TrustScoreCalculator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainServiceConfig {

    @Bean
    public CompatibilityEngine compatibilityEngine() {
        return new CompatibilityEngine();
    }

    @Bean
    public ImpulsivityEngine impulsivityEngine() {
        return new ImpulsivityEngine();
    }

    @Bean
    public TrustScoreCalculator trustScoreCalculator() {
        return new TrustScoreCalculator();
    }
}
