package com.tuan.ecommerce.modules.auth.infrastructure.persistence.role;

import com.tuan.ecommerce.modules.auth.domain.Role;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class RoleRepositoryAdapter implements RoleRepository {

    private final RoleJpaRepository roleJpaRepository;

    public RoleRepositoryAdapter(RoleJpaRepository roleJpaRepository) {
        this.roleJpaRepository = roleJpaRepository;
    }

    @Override
    public Optional<Role> findByName(String name) {
        return roleJpaRepository.findByName(name);
    }

    @Override
    public Role save(Role role) {
        return roleJpaRepository.save(role);
    }
}
