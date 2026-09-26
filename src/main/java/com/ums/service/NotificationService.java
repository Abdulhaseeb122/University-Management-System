package com.ums.service;

import com.ums.dto.NotificationResponse;

import java.util.List;

public interface NotificationService {

    List<NotificationResponse> getMyNotifications(String userEmail);
    List<NotificationResponse> getMyUnreadNotifications(String userEmail);
    long getMyUnreadCount(String userEmail);
    NotificationResponse markAsRead(Long notificationId, String userEmail);
    int markAllAsRead(String userEmail);
}