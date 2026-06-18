package com.autowash.repository;

import com.autowash.entity.LoyaltyProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoyaltyProfileRepository extends JpaRepository<LoyaltyProfile, Long> {
    Optional<LoyaltyProfile> findByUserUserId(Long userId);
}