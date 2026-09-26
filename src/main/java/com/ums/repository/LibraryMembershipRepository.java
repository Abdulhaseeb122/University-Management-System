package com.ums.repository;

import com.ums.entity.LibraryMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LibraryMembershipRepository extends JpaRepository<LibraryMembership, Long> {

    Optional<LibraryMembership> findByUserId(Long userId);

    Optional<LibraryMembership> findByLibraryCardNumber(String cardNumber);

    boolean existsByUserId(Long userId);

    boolean existsByLibraryCardNumber(String cardNumber);
}