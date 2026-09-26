package com.ums.repository;

import com.ums.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    // For users: fetch announcements targeted to their role OR to ALL
    List<Announcement> findByTargetRoleInOrderByCreatedAtDesc(List<String> targetRoles);

    // For admins: all announcements
    List<Announcement> findAllByOrderByCreatedAtDesc();
}