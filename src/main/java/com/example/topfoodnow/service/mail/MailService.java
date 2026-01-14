package com.example.topfoodnow.service.mail;

public interface MailService {
    void sendVerificationEmail(String recipientEmail, String userName, String verificationCode);
    void sendPasswordResetEmail(String toEmail, String userName, String resetLink);
}
