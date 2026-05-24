package com.matchpet.domain.service;

import com.matchpet.domain.model.OnboardingForm;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TrustScoreCalculatorTest {

    private final TrustScoreCalculator calculator = new TrustScoreCalculator();

    @Test
    void shouldReturnMaxScoreForIdealAnswers() {
        OnboardingForm form = new OnboardingForm(
                "PATIO_CERRADO",
                4,
                true,
                true
        );

        int score = calculator.calculate(form);

        assertEquals(100, score);
    }

    @Test
    void shouldReturnZeroIfControlVisitsAreRejected() {
        OnboardingForm form = new OnboardingForm(
                "PATIO_CERRADO",
                5,
                true,
                false
        );

        int score = calculator.calculate(form);

        assertEquals(0, score);
    }

    @Test
    void shouldCalculateScoreForMinimallyAcceptableForm() {
        OnboardingForm form = new OnboardingForm(
                "DEPARTAMENTO",
                2,
                false,
                true
        );

        // Vivienda: DEPARTAMENTO (+15)
        // Tiempo: 2h (<4h) (+10)
        // Experiencia: No (+10)
        // Visitas: Si (+20)
        // Total esperado: 15 + 10 + 10 + 20 = 55
        int score = calculator.calculate(form);

        assertEquals(55, score);
    }

    @Test
    void shouldRejectNullForm() {
        assertThrows(IllegalArgumentException.class, () -> calculator.calculate(null));
    }
}
