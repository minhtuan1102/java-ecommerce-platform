package com.tuan.ecommerce.modules.user.infrastructure.persistence;

import com.tuan.ecommerce.modules.user.domain.UserAddress;

import java.util.List;
import java.util.Optional;

public interface UserAddressRepository {
    UserAddress save(UserAddress address);
    Optional<UserAddress> findById(Long id);
    List<UserAddress> findByUserId(Long userId);
    void delete(UserAddress address);
    int clearDefaultForUser(Long userId);
}

