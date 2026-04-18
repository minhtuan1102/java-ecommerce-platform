package com.tuan.ecommerce.modules.auth.infrastructure.persistence.user;

import com.tuan.ecommerce.modules.auth.domain.User;

import java.util.Optional;

public interface UserRepository {

    User save(User user);
    
    Optional<User> findById(Long id);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    Optional<User> findByEmailIgnoreCase(String email);
}
