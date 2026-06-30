package com.noman.coverletter.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String toEmail, String token) {
        String link = baseUrl + "/verify-email?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Verify your Cover Letter AI account");
        message.setText(
            "Hello,\n\n" +
            "Thank you for registering with Cover Letter AI.\n\n" +
            "Please click the link below to verify your email address:\n\n" +
            link + "\n\n" +
            "This link will expire in 24 hours.\n\n" +
            "If you did not register, please ignore this email.\n\n" +
            "Best regards,\nCover Letter AI Team"
        );

        mailSender.send(message);
    }

    public void sendPasswordResetEmail(String toEmail, String token) {
        String link = baseUrl + "/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Reset your Cover Letter AI password");
        message.setText(
            "Hello,\n\n" +
            "We received a request to reset your password.\n\n" +
            "Click the link below to set a new password:\n\n" +
            link + "\n\n" +
            "This link will expire in 1 hour.\n\n" +
            "If you did not request a password reset, please ignore this email.\n\n" +
            "Best regards,\nCover Letter AI Team"
        );

        mailSender.send(message);
    }
}