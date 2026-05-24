package com.matchpet.domain.service;

import com.matchpet.domain.exception.ImpulsiveBehaviorException;

public class ImpulsivityEngine {

    private static final int MAX_LIKES_PER_MINUTE = 10;

    public void checkImpulsivity(long recentLikesCount) {
        if (recentLikesCount >= MAX_LIKES_PER_MINUTE) {
            throw new ImpulsiveBehaviorException(
                    "Impulsive behavior detected: maximum " + MAX_LIKES_PER_MINUTE + " likes per minute allowed"
            );
        }
    }
}
