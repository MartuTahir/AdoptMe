package com.matchpet.application.ports.input;

import com.matchpet.application.ports.input.dto.SwipeCommand;
import com.matchpet.application.ports.input.dto.SwipeResult;

public interface SwipeUseCase {

    SwipeResult execute(SwipeCommand command);
}
