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
        // 1. 加密密碼
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 2. 生成驗證碼
        String verificationCode = UUID.randomUUID().toString();
        user.setVerificationCode(verificationCode);

        // 3. 設定帳戶為未啟用
        user.setEnabled(false);

        // 4. 儲存用戶
        UserModel savedUser = userRepository.save(user);

        // 5. 寄送驗證信 (非同步執行可以避免阻塞主線程)
        // Spring @Async 可以在這裡使用，但需要額外配置
        // 為了簡潔，這裡直接呼叫，如果郵件發送慢可能影響用戶體驗
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

        // 將 Token 和過期時間直接存入 UserModel
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
            return Optional.empty(); // Token 不存在或不匹配
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
        // 密碼重設成功後，清除 Token 和過期時間
        user.setResetPasswordToken(null);
        user.setResetPasswordExpiryDate(null);
        userRepository.save(user);
        System.out.println("用戶 " + user.getEmail() + " 的密碼已更新。");
    }
}