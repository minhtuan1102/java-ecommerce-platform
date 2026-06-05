package com.tuan.ecommerce.modules.notification.infrastructure.persistence;

import com.tuan.ecommerce.modules.notification.domain.Notification;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository {
    Notification save(Notification notification);
    Optional<Notification> findById(Long id);
    List<Notification> findByUserId(Long userId);
}

