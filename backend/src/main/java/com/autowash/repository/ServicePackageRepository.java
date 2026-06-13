package com.autowash.repository;

import com.autowash.entity.ServicePackage;
import com.autowash.enums.PackageCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServicePackageRepository extends JpaRepository<ServicePackage, Long> {
    Optional<ServicePackage> findByPackageCode(PackageCode packageCode);
    boolean existsByPackageCode(PackageCode packageCode);
}
