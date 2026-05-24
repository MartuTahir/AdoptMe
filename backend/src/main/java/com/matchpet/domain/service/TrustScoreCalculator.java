package com.matchpet.domain.service;

import com.matchpet.domain.model.OnboardingForm;

public class TrustScoreCalculator {

    public int calculate(OnboardingForm form) {
        if (form == null) {
            throw new IllegalArgumentException("Onboarding form is required");
        }

        if (!form.acceptsControlVisits()) {
            return 0;
        }

        int score = 0;

        if ("PATIO_CERRADO".equalsIgnoreCase(form.housingType())) {
            score += 30;
        } else {
            score += 15;
        }

        if (form.availableHours() >= 4) {
            score += 30;
        } else {
            score += 10;
        }

        if (form.hasPreviousExperience()) {
            score += 20;
        } else {
            score += 10;
        }

        score += 20; // Aceptación de visitas de control suma 20 puntos

        return score;
    }
}
