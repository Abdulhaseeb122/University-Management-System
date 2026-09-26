package com.ums.controller;

import com.ums.dto.AnnouncementResponse;
import com.ums.dto.NotificationResponse;
import com.ums.service.AnnouncementService;
import com.ums.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final AnnouncementService announcementService;

    public NotificationController(NotificationService notificationService,
                                  AnnouncementService announcementService) {
        this.notificationService = notificationService;
        this.announcementService = announcementService;
    }

    // ---------- Notifications (inbox) ----------

    @GetMapping("/my")
    public ResponseEntity<List<NotificationResponse>> myNotifications(Principal principal) {
        return ResponseEntity.ok(notificationService.getMyNotifications(principal.getName()));
    }

    @GetMapping("/my/unread")
    public ResponseEntity<List<NotificationResponse>> myUnread(Principal principal) {
        return ResponseEntity.ok(notificationService.getMyUnreadNotifications(principal.getName()));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> unreadCount(Principal principal) {
        long count = notificationService.getMyUnreadCount(principal.getName());
        Map<String, Long> resp = new HashMap<>();
        resp.put("unreadCount", count);
        return ResponseEntity.ok(resp);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markRead(
            @PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(notificationService.markAsRead(id, principal.getName()));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Map<String, Integer>> markAllRead(Principal principal) {
        int updated = notificationService.markAllAsRead(principal.getName());
        Map<String, Integer> resp = new HashMap<>();
        resp.put("updated", updated);
        return ResponseEntity.ok(resp);
    }

    // ---------- Announcements (read-only for all authenticated users) ----------

    @GetMapping("/announcements")
    public ResponseEntity<List<AnnouncementResponse>> announcementsForMe(Principal principal) {
        return ResponseEntity.ok(announcementService.getAnnouncementsForMe(principal.getName()));
    }
}