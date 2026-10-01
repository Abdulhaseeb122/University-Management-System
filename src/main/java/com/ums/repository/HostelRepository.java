package com.ums.repository;

import com.ums.entity.Hostel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HostelRepository extends JpaRepository<Hostel, Long> {

    List<Hostel> findByCampusId(Long campusId);

    List<Hostel> findByType(Hostel.HostelType type);
}