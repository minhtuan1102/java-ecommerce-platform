package com.tuan.ecommerce.modules.auth.infrastructure.persistence.refreshtoken;

import com.tuan.ecommerce.modules.auth.domain.RefreshToken;
import com.tuan.ecommerce.modules.auth.domain.User;

import java.util.Optional;

public interface RefreshTokenRepository {
    Optional<RefreshToken> findByToken(String token);
    RefreshToken save(RefreshToken refreshToken);
    void deleteByUser(User user);
}
