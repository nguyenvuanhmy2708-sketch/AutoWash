package com.autowash.repository;

import com.autowash.entity.LoyaltyProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoyaltyProfileRepository extends JpaRepository<LoyaltyProfile, Long> {
}