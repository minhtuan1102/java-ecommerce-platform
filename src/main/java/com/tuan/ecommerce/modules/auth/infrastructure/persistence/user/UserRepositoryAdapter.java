package com.tuan.ecommerce.modules.auth.infrastructure.persistence.user;

import com.tuan.ecommerce.modules.auth.domain.User;
import org.springframework.stereotype.Repository;

import java.util.List;
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
    public Optional<User> findById(Long id) {
        return userJpaRepository.findById(id);
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
    public boolean existsByUsernameIgnoreCaseAndIdNot(String username, Long id) {
        return userJpaRepository.existsByUsernameIgnoreCaseAndIdNot(username, id);
    }

    @Override
    public boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id) {
        return userJpaRepository.existsByEmailIgnoreCaseAndIdNot(email, id);
    }

    @Override
    public Optional<User> findByEmailIgnoreCase(String email) {
        return userJpaRepository.findByEmailIgnoreCase(email);
    }

    @Override
    public List<User> findAll() {
        return userJpaRepository.findAll();
    }
}

