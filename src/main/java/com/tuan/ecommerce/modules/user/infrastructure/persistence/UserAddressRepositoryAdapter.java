package com.tuan.ecommerce.modules.user.infrastructure.persistence;

import com.tuan.ecommerce.modules.user.domain.UserAddress;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserAddressRepositoryAdapter implements UserAddressRepository {

    private final UserAddressJpaRepository jpaRepository;

    public UserAddressRepositoryAdapter(UserAddressJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public UserAddress save(UserAddress address) {
        return jpaRepository.save(address);
    }

    @Override
    public Optional<UserAddress> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<UserAddress> findByUserId(Long userId) {
        return jpaRepository.findByUserIdOrderByIdDesc(userId);
    }

    @Override
    public void delete(UserAddress address) {
        jpaRepository.delete(address);
    }

    @Override
    public int clearDefaultForUser(Long userId) {
        return jpaRepository.clearDefaultForUser(userId);
    }
}

