package com.tuan.ecommerce.modules.notification.infrastructure.persistence;

import com.tuan.ecommerce.modules.notification.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationJpaRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
}

