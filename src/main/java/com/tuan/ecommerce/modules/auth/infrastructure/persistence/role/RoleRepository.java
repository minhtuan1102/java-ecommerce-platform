package com.tuan.ecommerce.modules.auth.infrastructure.persistence.role;

import com.tuan.ecommerce.modules.auth.domain.Role;

import java.util.Optional;

public interface RoleRepository {
    Optional<Role> findByName(String name);
    Role save(Role role);
}
