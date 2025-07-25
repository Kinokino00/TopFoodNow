package com.example.topfoodnow.service;

public interface MailService {
    void sendVerificationEmail(String toEmail, String userName, String verificationCode);
    void sendPasswordResetEmail(String toEmail, String userName, String resetLink);
    void sendEmail(String toEmail, String subject, String content);
}