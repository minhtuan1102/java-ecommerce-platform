package com.tuan.ecommerce.modules.auth.infrastructure;

import com.tuan.ecommerce.modules.auth.domain.User;

import java.util.Optional;

public interface UserRepository {

    User save(User user);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    Optional<User> findByEmailIgnoreCase(String email);
}
