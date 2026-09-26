package com.ums.service;

import com.ums.dto.AnnouncementRequest;
import com.ums.dto.AnnouncementResponse;

import java.util.List;

public interface AnnouncementService {

    // Admin
    AnnouncementResponse createAnnouncement(String adminEmail, AnnouncementRequest request);
    List<AnnouncementResponse> getAllAnnouncements();
    AnnouncementResponse getAnnouncementById(Long id);
    AnnouncementResponse updateAnnouncement(Long id, AnnouncementRequest request);
    void deleteAnnouncement(Long id);

    // User
    List<AnnouncementResponse> getAnnouncementsForMe(String userEmail);
}