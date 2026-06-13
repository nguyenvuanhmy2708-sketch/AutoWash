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

    public void sendResetPasswordEmail(String toEmail, String resetLink) {
        log.info("---------------- RESET PASSWORD LINK ----------------");
        log.info("Email to: {}", toEmail);
        log.info("Link: {}", resetLink);
        log.info("-----------------------------------------------------");

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("autowash.service@gmail.com");
            message.setTo(toEmail);
            message.setSubject("Reset Password Request - AutoWash");
            message.setText("Chào bạn,\n\n"
                    + "Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản AutoWash gắn liền với email này.\n"
                    + "Vui lòng truy cập đường dẫn sau để đặt lại mật khẩu (đường dẫn có hiệu lực trong 15 phút):\n\n"
                    + resetLink + "\n\n"
                    + "Nếu bạn không yêu cầu đặt lại mật khẩu, bạn có thể bỏ qua email này.\n\n"
                    + "Trân trọng,\n"
                    + "AutoWash Team.");

            mailSender.send(message);
            log.info("Email sent successfully to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send email to {} via SMTP server. Error: {}", toEmail, e.getMessage());
            log.warn("Ensure SMTP properties in application.yml are configured correctly. You can still use the link logged above to test the resetting functionality.");
        }
    }
}
