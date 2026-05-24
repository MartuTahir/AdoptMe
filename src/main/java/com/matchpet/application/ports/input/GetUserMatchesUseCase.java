package com.matchpet.application.ports.input;

import com.matchpet.application.ports.input.dto.MatchResult;

import java.util.List;

public interface GetUserMatchesUseCase {

    List<MatchResult> execute(String userId);
}
