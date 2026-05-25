package com.matchpet.application.ports.input;

import com.matchpet.application.ports.input.dto.PetResult;
import org.springframework.data.domain.Page;

public interface ListPetsUseCase {

    Page<PetResult> execute(int page, int size);
}
