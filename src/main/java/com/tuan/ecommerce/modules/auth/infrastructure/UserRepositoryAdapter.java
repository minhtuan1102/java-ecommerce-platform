package com.tuan.ecommerce.modules.auth.infrastructure;

import com.tuan.ecommerce.modules.auth.domain.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    public UserRepositoryAdapter(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public User save(User user) {
        return userJpaRepository.save(user);
    }

    @Override
    public boolean existsByUsernameIgnoreCase(String username) {
        return userJpaRepository.existsByUsernameIgnoreCase(username);
    }

    @Override
    public boolean existsByEmailIgnoreCase(String email) {
        return userJpaRepository.existsByEmailIgnoreCase(email);
    }

    @Override
    public Optional<User> findByEmailIgnoreCase(String email) {
        return userJpaRepository.findByEmailIgnoreCase(email);
    }
}

