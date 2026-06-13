package com.autowash.controller;

import com.autowash.entity.ServicePackage;
import com.autowash.service.ServicePackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/packages")
@RequiredArgsConstructor
public class ServicePackageController {

    private final ServicePackageService servicePackageService;

    @GetMapping
    public ResponseEntity<List<ServicePackage>> getAllPackages() {
        return ResponseEntity.ok(servicePackageService.getAllPackages());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServicePackage> getPackageById(@PathVariable Long id) {
        return ResponseEntity.ok(servicePackageService.getPackageById(id));
    }

    @PostMapping
    public ResponseEntity<ServicePackage> createPackage(@RequestBody ServicePackage servicePackage) {
        return new ResponseEntity<>(servicePackageService.createPackage(servicePackage), HttpStatus.CREATED);
    }
}
