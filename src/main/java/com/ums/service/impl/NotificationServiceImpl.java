package com.ums.service.impl;

import com.ums.dto.NotificationResponse;
import com.ums.entity.Notification;
import com.ums.entity.User;
import com.ums.exception.BadRequestException;
import com.ums.exception.ResourceNotFoundException;
import com.ums.repository.NotificationRepository;
import com.ums.repository.UserRepository;
import com.ums.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                   UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<NotificationResponse> getMyNotifications(String userEmail) {
        User user = getUser(userEmail);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<NotificationResponse> getMyUnreadNotifications(String userEmail) {
        User user = getUser(userEmail);
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(user.getId())
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public long getMyUnreadCount(String userEmail) {
        User user = getUser(userEmail);
        return notificationRepository.countByUserIdAndIsReadFalse(user.getId());
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long notificationId, String userEmail) {
        User user = getUser(userEmail);

        Notification n = notificationRepository.findByIdAndUserId(notificationId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notification not found or does not belong to you."));

        n.setIsRead(true);
        return mapToResponse(notificationRepository.save(n));
    }

    @Override
    @Transactional
    public int markAllAsRead(String userEmail) {
        User user = getUser(userEmail);
        return notificationRepository.markAllAsRead(user.getId());
    }

    // ---------------- Helpers ----------------

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private NotificationResponse mapToResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .isRead(n.getIsRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}