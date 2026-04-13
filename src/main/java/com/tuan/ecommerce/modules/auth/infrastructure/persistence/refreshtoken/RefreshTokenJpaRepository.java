package com.tuan.ecommerce.modules.auth.infrastructure.persistence.refreshtoken;

import com.tuan.ecommerce.modules.auth.domain.RefreshToken;
import com.tuan.ecommerce.modules.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(User user);
}
