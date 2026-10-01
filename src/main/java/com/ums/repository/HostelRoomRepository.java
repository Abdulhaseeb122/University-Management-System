package com.ums.repository;

import com.ums.entity.HostelRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HostelRoomRepository extends JpaRepository<HostelRoom, Long> {

    List<HostelRoom> findByHostelId(Long hostelId);

    boolean existsByHostelId(Long hostelId);
}