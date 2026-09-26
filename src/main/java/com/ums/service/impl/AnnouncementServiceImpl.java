package com.ums.service.impl;

import com.ums.dto.AnnouncementRequest;
import com.ums.dto.AnnouncementResponse;
import com.ums.entity.Announcement;
import com.ums.entity.Notification;
import com.ums.entity.Role;
import com.ums.entity.User;
import com.ums.exception.BadRequestException;
import com.ums.exception.ResourceNotFoundException;
import com.ums.repository.AnnouncementRepository;
import com.ums.repository.NotificationRepository;
import com.ums.repository.RoleRepository;
import com.ums.repository.UserRepository;
import com.ums.service.AnnouncementService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public AnnouncementServiceImpl(AnnouncementRepository announcementRepository,
                                   NotificationRepository notificationRepository,
                                   UserRepository userRepository,
                                   RoleRepository roleRepository) {
        this.announcementRepository = announcementRepository;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public AnnouncementResponse createAnnouncement(String adminEmail, AnnouncementRequest request) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found: " + adminEmail));

        // If targetRole is specific (not ALL), it must exist in roles table
        if (!"ALL".equals(request.getTargetRole())) {
            Role role = roleRepository.findByName(request.getTargetRole())
                    .orElseThrow(() -> new BadRequestException(
                            "Target role does not exist: " + request.getTargetRole()));
        }

        Announcement a = new Announcement();
        a.setTitle(request.getTitle());
        a.setContent(request.getContent());
        a.setTargetRole(request.getTargetRole() != null ? request.getTargetRole() : "ALL");
        a.setCreatedBy(admin);

        Announcement saved = announcementRepository.save(a);

        // Auto-generate notifications for the targeted users
        generateNotifications(saved);

        return mapToResponse(saved);
    }

    @Override
    public List<AnnouncementResponse> getAllAnnouncements() {
        return announcementRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public AnnouncementResponse getAnnouncementById(Long id) {
        Announcement a = announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found: " + id));
        return mapToResponse(a);
    }

    @Override
    @Transactional
    public AnnouncementResponse updateAnnouncement(Long id, AnnouncementRequest request) {
        Announcement a = announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found: " + id));

        if (!"ALL".equals(request.getTargetRole())) {
            roleRepository.findByName(request.getTargetRole())
                    .orElseThrow(() -> new BadRequestException(
                            "Target role does not exist: " + request.getTargetRole()));
        }

        a.setTitle(request.getTitle());
        a.setContent(request.getContent());
        a.setTargetRole(request.getTargetRole() != null ? request.getTargetRole() : a.getTargetRole());

        return mapToResponse(announcementRepository.save(a));
    }

    @Override
    @Transactional
    public void deleteAnnouncement(Long id) {
        Announcement a = announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found: " + id));
        announcementRepository.delete(a);
    }

    @Override
    public List<AnnouncementResponse> getAnnouncementsForMe(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        // Fetch announcements targeted to ALL or the user's specific role
        List<String> targetRoles = List.of("ALL", user.getRole().getName());

        return announcementRepository.findByTargetRoleInOrderByCreatedAtDesc(targetRoles)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ---------------- Helpers ----------------

    private void generateNotifications(Announcement a) {
        List<User> recipients;
        if ("ALL".equals(a.getTargetRole())) {
            recipients = userRepository.findAll();
        } else {
            Role role = roleRepository.findByName(a.getTargetRole()).orElse(null);
            if (role == null) return;
            recipients = userRepository.findAll().stream()
                    .filter(u -> u.getRole().getId().equals(role.getId()))
                    .collect(Collectors.toList());
        }

        for (User u : recipients) {
            Notification n = new Notification();
            n.setUser(u);
            n.setTitle(a.getTitle());
            n.setMessage(a.getContent());
            n.setIsRead(false);
            notificationRepository.save(n);
        }
    }

    private AnnouncementResponse mapToResponse(Announcement a) {
        User u = a.getCreatedBy();
        return AnnouncementResponse.builder()
                .id(a.getId())
                .title(a.getTitle())
                .content(a.getContent())
                .targetRole(a.getTargetRole())
                .createdAt(a.getCreatedAt())
                .createdById(u != null ? u.getId() : null)
                .createdByName(u != null ? u.getFirstName() + " " + u.getLastName() : "System")
                .build();
    }
}