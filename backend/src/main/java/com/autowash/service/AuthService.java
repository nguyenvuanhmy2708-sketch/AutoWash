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
import org.springframework.beans.factory.annotation.Value;
import com.autowash.dto.ForgotPasswordRequest;
import com.autowash.dto.ResetPasswordRequest;
import com.autowash.entity.PasswordResetToken;
import com.autowash.repository.PasswordResetTokenRepository;
import java.time.LocalDateTime;
<<<<<<< HEAD
import java.util.UUID;

=======
import java.security.SecureRandom;
>>>>>>> main
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final LoyaltyProfileRepository loyaltyProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
<<<<<<< HEAD
        // Validation Checks
=======
>>>>>>> main
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

<<<<<<< HEAD
        // Email uniqueness validation
=======
>>>>>>> main
        if (userRepository.existsByEmail(request.getEmail().trim())) {
            throw new AppException("Email is already registered", HttpStatus.BAD_REQUEST);
        }

<<<<<<< HEAD
        // Phone number uniqueness validation
=======
>>>>>>> main
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber().trim())) {
            throw new AppException("Phone number is already registered", HttpStatus.BAD_REQUEST);
        }

<<<<<<< HEAD
        // Create and save User
=======
>>>>>>> main
        User user = User.builder()
                .fullName(request.getFullName().trim())
                .phoneNumber(request.getPhoneNumber().trim())
                .email(request.getEmail().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.CUSTOMER)
                .isActive(true)
                .build();

        user = userRepository.save(user);

<<<<<<< HEAD
        // Create and save Wallet
=======
>>>>>>> main
        Wallet wallet = Wallet.builder()
                .user(user)
                .balance(BigDecimal.ZERO)
                .build();
        walletRepository.save(wallet);

<<<<<<< HEAD
        // Create and save Loyalty Profile
=======
>>>>>>> main
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

<<<<<<< HEAD
=======
        // ĐÃ SỬA: Bỏ dấu ngoặc thừa ở cuối dòng này gây ra 3 lỗi compile
>>>>>>> main
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

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new AppException("Email is required", HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.findByEmail(request.getEmail().trim())
                .orElseThrow(() -> new AppException("Email is not registered", HttpStatus.NOT_FOUND));

<<<<<<< HEAD
        // Delete existing tokens for this user
        tokenRepository.deleteByUser(user);

        // Generate token
        String token = UUID.randomUUID().toString();
=======
        tokenRepository.deleteByUser(user);

        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000);
        String token = String.valueOf(code);
>>>>>>> main
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(15))
                .build();

        tokenRepository.save(resetToken);

<<<<<<< HEAD
        // Send email
        String resetLink = frontendUrl + "/reset-password?token=" + token;
        emailService.sendResetPasswordEmail(user.getEmail(), resetLink);
=======
        // ĐÃ SỬA: Đổi tên hàm sendResetPasswordCode thành sendResetPasswordEmail để khớp với file EmailService của bạn
        emailService.sendResetPasswordEmail(user.getEmail(), token);
>>>>>>> main
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (request.getToken() == null || request.getToken().trim().isEmpty()) {
            throw new AppException("Token is required", HttpStatus.BAD_REQUEST);
        }
        if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
            throw new AppException("Password is required", HttpStatus.BAD_REQUEST);
        }

        PasswordResetToken resetToken = tokenRepository.findByToken(request.getToken().trim())
                .orElseThrow(() -> new AppException("Invalid or expired password reset token", HttpStatus.BAD_REQUEST));

        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken);
            throw new AppException("Password reset token has expired", HttpStatus.BAD_REQUEST);
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

<<<<<<< HEAD
        // Clean up token
=======
>>>>>>> main
        tokenRepository.delete(resetToken);
    }
}