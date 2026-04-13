package com.tuan.ecommerce.modules.auth.infrastructure.persistence.role;

import com.tuan.ecommerce.modules.auth.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleJpaRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
}
