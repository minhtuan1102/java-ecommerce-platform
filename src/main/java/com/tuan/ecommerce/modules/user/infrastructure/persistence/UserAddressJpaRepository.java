package com.tuan.ecommerce.modules.user.infrastructure.persistence;

import com.tuan.ecommerce.modules.user.domain.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserAddressJpaRepository extends JpaRepository<UserAddress, Long> {
    List<UserAddress> findByUserIdOrderByIdDesc(Long userId);

    @Modifying
    @Query("UPDATE UserAddress ua SET ua.defaultAddress = false WHERE ua.user.id = :userId")
    int clearDefaultForUser(@Param("userId") Long userId);
}

