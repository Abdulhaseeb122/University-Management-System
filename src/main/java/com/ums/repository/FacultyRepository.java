package com.ums.repository;
import com.ums.entity.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
public interface FacultyRepository extends JpaRepository<Faculty, Long> {

}