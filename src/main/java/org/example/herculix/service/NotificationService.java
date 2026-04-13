package org.example.herculix.service;

import org.example.herculix.entity.Notification;
import org.example.herculix.model.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    
    PageResponse<Notification> getUserNotifications(String userId, Pageable pageable);
    
    void markAsRead(String notificationId, String userId);
    
    void markAllAsRead(String userId);
    
    void deleteNotification(String notificationId, String userId);
    
    long getUnreadCount(String userId);
}
