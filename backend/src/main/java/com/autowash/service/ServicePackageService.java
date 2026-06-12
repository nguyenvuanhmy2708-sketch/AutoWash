package com.autowash.service;

import com.autowash.entity.ServicePackage;
import com.autowash.exception.AppException;
import com.autowash.repository.ServicePackageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServicePackageService {

    private final ServicePackageRepository servicePackageRepository;

    public List<ServicePackage> getAllPackages() {
        return servicePackageRepository.findAll();
    }

    public ServicePackage getPackageById(Long packageId) {
        return servicePackageRepository.findById(packageId)
                .orElseThrow(() -> new AppException("Service package not found", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public ServicePackage createPackage(ServicePackage servicePackage) {
        if (servicePackageRepository.existsByPackageCode(servicePackage.getPackageCode())) {
            throw new AppException("Service package code already exists", HttpStatus.BAD_REQUEST);
        }
        return servicePackageRepository.save(servicePackage);
    }
}
