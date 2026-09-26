package com.ums.repository;

import com.ums.entity.BookIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookIssueRepository extends JpaRepository<BookIssue, Long> {

    List<BookIssue> findByUserId(Long userId);

    List<BookIssue> findByUserIdAndReturnDateIsNull(Long userId);

    List<BookIssue> findByReturnDateIsNull();

    List<BookIssue> findByReturnDateIsNullAndDueDateBefore(LocalDate date);

    Optional<BookIssue> findByBookIdAndUserIdAndReturnDateIsNull(Long bookId, Long userId);

    long countByUserIdAndReturnDateIsNull(Long userId);
}