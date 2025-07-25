package com.example.topfoodnow.service.impl;

import com.example.topfoodnow.controller.CategoryController;
import com.example.topfoodnow.model.UserModel;
import com.example.topfoodnow.model.RoleModel;
import com.example.topfoodnow.service.UserService;
import com.example.topfoodnow.service.MailService;
import com.example.topfoodnow.repository.UserRepository;
import com.example.topfoodnow.repository.RoleRepository;
import com.example.topfoodnow.dto.UserProfileUpdateDTO;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.slf4j.Logger;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);

    @Value("${app.base-url}")
    private String appBaseUrl;

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;
    private MailService mailService;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 根據用戶 ID 查找用戶
     * @param id 用戶 ID
     * @return 如果找到用戶則返回 Optional<UserModel>，否則返回 Optional.empty()
     */
    @Override
    public Optional<UserModel> findById(Integer id) {
        return userRepository.findById(id);
    }

    /**
     * 根據電子郵件查找用戶
     * @param email 電子郵件地址
     * @return 如果找到用戶則返回 Optional<UserModel>，否則返回 Optional.empty()
     */
    @Override
    public Optional<UserModel> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * 註冊新用戶並寄送驗證信
     * @param user 待註冊的用戶模型
     * @return 儲存後的用戶模型
     */
    @Override
    @Transactional
    public UserModel addUser(UserModel user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        String verificationCode = UUID.randomUUID().toString();
        user.setVerificationCode(verificationCode);
        user.setEnabled(false);

        RoleModel userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Default USER role not found!"));
        user.setRole(userRole);
        UserModel savedUser = userRepository.save(user);
        mailService.sendVerificationEmail(user.getEmail(), user.getName(), verificationCode);
        return savedUser;
    }

    /**
     * 驗證用戶登入
     * @param email 用戶輸入的電子郵件
     * @param password 用戶輸入的密碼 (明文)
     * @return 如果驗證成功且帳戶已啟用，返回對應的 UserModel；否則返回 null
     */
    @Override
    public UserModel authenticate(String email, String password) {
        Optional<UserModel> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            UserModel user = userOptional.get();
            if (user.getEnabled() != null && !user.getEnabled()) {
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
    @Override
    @Transactional
    public boolean verifyAccount(String verificationCode) {
        Optional<UserModel> userOptional = userRepository.findByVerificationCode(verificationCode);

        if (userOptional.isPresent()) {
            UserModel user = userOptional.get();
            if (user.getEnabled() != null && !user.getEnabled()) {
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
     * @param userEmail
     * @return 生成的 Token 字符串
     */
    @Override
    @Transactional
    public void createPasswordResetTokenForUser(String userEmail) {
        Optional<UserModel> userOptional = userRepository.findByEmail(userEmail);
        if (userOptional.isEmpty()) {
            logger.warn("Password reset requested for non-existent email: {}", userEmail);
            return;
        }
        UserModel user = userOptional.get();

        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(24); // Token 有效期設定為 24 小時

        user.setResetPasswordToken(token);
        user.setResetPasswordExpiryDate(expiryDate);
        userRepository.save(user);

        String resetLink = appBaseUrl + "/reset-password?token=" + token;
        mailService.sendPasswordResetEmail(user.getEmail(), user.getName(), resetLink);
        logger.info("Generated password reset token for user: {}", user.getEmail());
    }

    /**
     * 驗證重設密碼 Token 的有效性
     * @param token 待驗證的 Token 字符串
     * @return 如果 Token 有效且未過期，返回對應的 UserModel；否則返回 Optional.empty()
     */
    @Override
    public String validatePasswordResetToken(String token) {
        Optional<UserModel> userOptional = userRepository.findByResetPasswordToken(token);
        if (userOptional.isEmpty()) {
            return "invalid";
        }

        UserModel user = userOptional.get();
        if (user.getResetPasswordExpiryDate() == null || user.getResetPasswordExpiryDate().isBefore(LocalDateTime.now())) {
            return "expired";
        }
        return "valid";
    }

    /**
     * 更改用戶密碼
     * @param token
     * @param newPassword 新密碼 (明文)
     */
    @Override
    @Transactional
    public void changeUserPassword(String token, String newPassword) {
        Optional<UserModel> userOptional = userRepository.findByResetPasswordToken(token);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("Invalid password reset token.");
        }

        UserModel user = userOptional.get();

        // 再次檢查 Token 是否過期，以防萬一（雖然前端驗證過，但仍需後端雙重檢查）
        if (user.getResetPasswordExpiryDate() == null || user.getResetPasswordExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Password reset token has expired.");
        }

        user.setPassword(passwordEncoder.encode(newPassword)); // 加密新密碼
        user.setResetPasswordToken(null); // 清除 Token
        user.setResetPasswordExpiryDate(null); // 清除 Token 過期時間
        userRepository.save(user);
    }


    /**
     * 更新用戶個人資料 (僅限名稱、YouTube 和 Instagram 連結)
     * @param userId 要更新的用戶 ID
     * @param updateDTO 包含更新資訊的 DTO
     * @return 更新後的 UserModel
     */
    @Override
    @Transactional
    public UserModel updateProfile(Integer userId, UserProfileUpdateDTO updateDTO) {
        UserModel user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("用戶未找到，ID: " + userId));
        user.setName(updateDTO.getName());
        user.setYtUrl(updateDTO.getYtUrl());
        user.setIgUrl(updateDTO.getIgUrl());
        return userRepository.save(user);
    }
}