package com.example.topfoodnow.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.mail.MailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class MailService {
    private final JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String appBaseUrl;

    @Value("${spring.mail.username}")
    private String senderEmail;

    private static final Logger logger = LoggerFactory.getLogger(MailService.class);

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendVerificationEmail(String recipientEmail, String userName, String verificationCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail);
        message.setTo(recipientEmail);
        message.setSubject("TopFoodNow - 請驗證您的帳戶");

        String verificationLink = appBaseUrl + "/verify?code=" + verificationCode;
        String emailContent = "哈囉 " + userName + ",\n\n"
            + "感謝您註冊 TopFoodNow！請點擊以下連結以開通您的帳戶：\n\n"
            + verificationLink + "\n\n"
            + "如果連結無效，請將它複製並貼到您的瀏覽器中。\n\n"
            + "祝您用餐愉快，\n"
            + "TopFoodNow 團隊敬上";
        message.setText(emailContent);

        try {
            mailSender.send(message);
            logger.info("驗證信已成功寄送至: {}", recipientEmail);
        } catch (MailException e) {
            logger.error("寄送驗證信失敗至 {}: {}", recipientEmail, e.getMessage(), e);
        }
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String userName, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail);
        message.setTo(toEmail);
        message.setSubject("密碼重設請求 - 餐廳推薦");
        message.setText("您好 " + userName + ",\n\n"
                + "我們收到您的密碼重設請求。請點擊以下連結重設您的密碼：\n"
                + resetLink + "\n\n"
                + "如果這不是您發出的請求，請忽略此郵件。\n\n"
                + "此連結將在 1 小時內失效。\n\n"
                + "祝您用餐愉快！\n"
                + "Top Food Now 團隊");
        try {
            mailSender.send(message);
            logger.info("密碼重設信已發送至: {}", toEmail);
        } catch (MailException e) {
            logger.error("發送密碼重設信失敗至 {}: {}", toEmail, e.getMessage(), e);
        }
    }
}
