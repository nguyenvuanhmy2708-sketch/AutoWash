package com.autowash.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendResetPasswordCode(String toEmail, String code) {
        log.info("---------------- RESET PASSWORD CODE ----------------");
        log.info("Email to: {}", toEmail);
        log.info("Code: {}", code);
        log.info("-----------------------------------------------------");

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("autowash.service@gmail.com");
            message.setTo(toEmail);
            message.setSubject("Reset Password Verification Code - AutoWash");
            message.setText("Chào bạn,\n\n"
                    + "Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản AutoWash gắn liền với email này.\n"
                    + "Mã xác nhận đặt lại mật khẩu của bạn là (mã này có hiệu lực trong 15 phút):\n\n"
                    + code + "\n\n"
                    + "Nếu bạn không yêu cầu đặt lại mật khẩu, bạn có thể bỏ qua email này.\n\n"
                    + "Trân trọng,\n"
                    + "AutoWash Team.");

            mailSender.send(message);
            log.info("Email sent successfully to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send email to {} via SMTP server. Error: {}", toEmail, e.getMessage());
            log.warn("Ensure SMTP properties in application.yml are configured correctly. You can still use the code logged above to test the resetting functionality.");
        }
    }
}
