package com.matchpet.application.ports.output;

import com.matchpet.domain.model.User;

import java.util.Optional;

public interface UserPersistencePort {

    Optional<User> findById(String id);

    User save(User user);
}
