package com.example.topfoodnow.service;

import com.example.topfoodnow.model.UserModel;
import com.example.topfoodnow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, MailService mailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
    }

    /**
     * 根據用戶 ID 查找用戶
     * @param id 用戶 ID
     * @return 如果找到用戶則返回 Optional<UserModel>，否則返回 Optional.empty()
     */
    public Optional<UserModel> findById(Integer id) {
        return userRepository.findById(id);
    }

    /**
     * 根據電子郵件查找用戶
     * @param email 電子郵件地址
     * @return 如果找到用戶則返回 Optional<UserModel>，否則返回 Optional.empty()
     */
    public Optional<UserModel> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * 註冊新用戶並寄送驗證信
     * @param user 待註冊的用戶模型
     * @return 儲存後的用戶模型
     */
    public UserModel addUser(UserModel user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        String verificationCode = UUID.randomUUID().toString();
        user.setVerificationCode(verificationCode);
        user.setEnabled(false);
        UserModel savedUser = userRepository.save(user);
        mailService.sendVerificationEmail(user.getEmail(), user.getUserName(), verificationCode);
        return savedUser;
    }

    /**
     * 驗證用戶登入
     * @param email 用戶輸入的電子郵件
     * @param password 用戶輸入的密碼 (明文)
     * @return 如果驗證成功且帳戶已啟用，返回對應的 UserModel；否則返回 null
     */
    public UserModel authenticate(String email, String password) {
        Optional<UserModel> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            UserModel user = userOptional.get();
            if (!user.isEnabled()) {
                System.out.println("嘗試登入但帳戶未啟用: " + email);
                return null;
            }
            if (passwordEncoder.matches(password, user.getPassword())) {
                return user;
            }
        }
        return null;
    }

    /**
     * 根據驗證碼啟用用戶帳戶
     * @param verificationCode 接收到的驗證碼
     * @return 如果成功啟用，返回 true；否則返回 false
     */
    public boolean verifyAccount(String verificationCode) {
        Optional<UserModel> userOptional = userRepository.findByVerificationCode(verificationCode);

        if (userOptional.isPresent()) {
            UserModel user = userOptional.get();
            if (!user.isEnabled()) {
                user.setEnabled(true);
                user.setVerificationCode(null);
                userRepository.save(user);
                return true;
            }
        }
        return false;
    }

    /**
     * 為用戶創建並保存重設密碼 Token
     * @param user 需要重設密碼的用戶
     * @return 生成的 Token 字符串
     */
    public String createPasswordResetTokenForUser(UserModel user) {
        String token = UUID.randomUUID().toString();

        user.setResetPasswordToken(token);
        user.setResetPasswordExpiryDate(LocalDateTime.now().plusHours(1)); // Token 1 小時後過期
        userRepository.save(user);

        System.out.println("生成並保存了重設密碼 token: " + token + " 給用戶: " + user.getEmail());
        return token;
    }

    /**
     * 驗證重設密碼 Token 的有效性
     * @param token 待驗證的 Token 字符串
     * @return 如果 Token 有效且未過期，返回對應的 UserModel；否則返回 Optional.empty()
     */
    public Optional<UserModel> validatePasswordResetToken(String token) {
        Optional<UserModel> userOptional = userRepository.findByResetPasswordToken(token);

        if (userOptional.isEmpty()) {
            System.out.println("Token 不存在或不匹配: " + token);
            return Optional.empty();
        }

        UserModel user = userOptional.get();
        if (user.isResetPasswordTokenExpired()) {
            System.out.println("Token 已過期: " + token);
            user.setResetPasswordToken(null);
            user.setResetPasswordExpiryDate(null);
            userRepository.save(user);
            return Optional.empty();
        }
        return Optional.of(user);
    }

    /**
     * 更改用戶密碼
     * @param user 需要更改密碼的用戶
     * @param newPassword 新密碼 (明文)
     */
    public void changeUserPassword(UserModel user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);
        user.setResetPasswordExpiryDate(null);
        userRepository.save(user);
        System.out.println("用戶 " + user.getEmail() + " 的密碼已更新。");
    }
}