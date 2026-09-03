package com.ums.repository;

import com.ums.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByRollNumber(String rollNumber);

    Boolean existsByRollNumber(String rollNumber);

    List<Student> findByDepartmentId(Long departmentId);

    @Query("SELECT s FROM Student s JOIN FETCH s.user LEFT JOIN FETCH s.department WHERE s.user.email = :email")
    Optional<Student> findByUserEmail(@Param("email") String email);
}