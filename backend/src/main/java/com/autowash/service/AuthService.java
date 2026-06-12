package com.autowash.service;

import com.autowash.dto.LoginRequest;
import com.autowash.dto.LoginResponse;
import com.autowash.dto.RegisterRequest;
import com.autowash.dto.RegisterResponse;
import com.autowash.entity.LoyaltyProfile;
import com.autowash.entity.User;
import com.autowash.entity.Wallet;
import com.autowash.enums.Role;
import com.autowash.enums.Tier;
import com.autowash.exception.AppException;
import com.autowash.repository.LoyaltyProfileRepository;
import com.autowash.repository.UserRepository;
import com.autowash.repository.WalletRepository;
import com.autowash.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final LoyaltyProfileRepository loyaltyProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        // Validation Checks
        if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
            throw new AppException("Full name is required", HttpStatus.BAD_REQUEST);
        }
        if (request.getPhoneNumber() == null || request.getPhoneNumber().trim().isEmpty()) {
            throw new AppException("Phone number is required", HttpStatus.BAD_REQUEST);
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new AppException("Email is required", HttpStatus.BAD_REQUEST);
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new AppException("Password is required", HttpStatus.BAD_REQUEST);
        }

        // Email uniqueness validation
        if (userRepository.existsByEmail(request.getEmail().trim())) {
            throw new AppException("Email is already registered", HttpStatus.BAD_REQUEST);
        }

        // Phone number uniqueness validation
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber().trim())) {
            throw new AppException("Phone number is already registered", HttpStatus.BAD_REQUEST);
        }

        // Create and save User
        User user = User.builder()
                .fullName(request.getFullName().trim())
                .phoneNumber(request.getPhoneNumber().trim())
                .email(request.getEmail().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.CUSTOMER)
                .isActive(true)
                .build();

        user = userRepository.save(user);

        // Create and save Wallet
        Wallet wallet = Wallet.builder()
                .user(user)
                .balance(BigDecimal.ZERO)
                .build();
        walletRepository.save(wallet);

        // Create and save Loyalty Profile
        LoyaltyProfile loyaltyProfile = LoyaltyProfile.builder()
                .user(user)
                .totalPoints(0)
                .currentTier(Tier.BRONZE)
                .build();
        loyaltyProfileRepository.save(loyaltyProfile);

        return RegisterResponse.builder()
                .message("Register successfully")
                .build();
    }

    public LoginResponse login(LoginRequest request) {
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new AppException("Email is required", HttpStatus.BAD_REQUEST);
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new AppException("Password is required", HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.findByEmail(request.getEmail().trim())
                .orElseThrow(() -> new AppException("Invalid email or password", HttpStatus.UNAUTHORIZED));

        if (!user.getIsActive()) {
            throw new AppException("User account is inactive", HttpStatus.FORBIDDEN);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AppException("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }

        String token = jwtTokenProvider.generateToken(user.getEmail());

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getUserId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }
}