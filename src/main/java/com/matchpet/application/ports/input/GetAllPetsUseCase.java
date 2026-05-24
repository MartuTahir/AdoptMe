package com.matchpet.application.ports.input;

import com.matchpet.application.ports.input.dto.PetResult;

import java.util.List;

public interface GetAllPetsUseCase {

    List<PetResult> execute(int page, int size);
}
