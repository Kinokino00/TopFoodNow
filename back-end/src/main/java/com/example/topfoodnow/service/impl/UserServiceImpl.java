package com.example.topfoodnow.service.impl;

import com.example.topfoodnow.model.UserModel;
import com.example.topfoodnow.model.RoleModel;
import com.example.topfoodnow.service.GcsService;
import com.example.topfoodnow.service.UserService;
import com.example.topfoodnow.service.MailService;
import com.example.topfoodnow.repository.RoleRepository;
import com.example.topfoodnow.repository.UserRepository;
import com.example.topfoodnow.dto.UserProfileUpdateDTO;
import com.example.topfoodnow.dto.UserProfileResponseDTO;
import com.example.topfoodnow.dto.UserRegisterRequestDTO;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.io.IOException;

@Service
public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Value("${app.base-url}")
    private String appBaseUrl;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MailService mailService;

    @Autowired
    private GcsService gcsService;

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
     * 新增的 save 方法實現
     * @param user 待保存的用戶模型
     * @return 保存後的用戶模型
     */
    @Override
    @Transactional // 添加事務管理
    public UserModel save(UserModel user) {
        return userRepository.save(user);
    }

    /**
     * 註冊新用戶並寄送驗證信
     * @param requestDTO 待註冊的用戶模型
     * @return 儲存後的用戶模型
     */
    @Override
    @Transactional
    public UserModel addUser(UserRegisterRequestDTO requestDTO) throws IOException {
        UserModel user = new UserModel();
        user.setEmail(requestDTO.getEmail());
        user.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
        user.setName(requestDTO.getName());
        user.setYtUrl(requestDTO.getYtUrl());
        user.setIgUrl(requestDTO.getIgUrl());
        user.setEnabled(false); // 預設為未啟用
        user.setVerificationCode(UUID.randomUUID().toString()); // 生成驗證碼

        RoleModel userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new EntityNotFoundException("Role 'USER' not found. Please ensure default roles are set up."));
        user.setRole(userRole);

        // 處理頭像上傳 (註冊)
        if (requestDTO.getProfilePictureFile() != null && !requestDTO.getProfilePictureFile().isEmpty()) {
            String profilePicUrl = gcsService.uploadFile(requestDTO.getProfilePictureFile(), "profile-pictures/");
            user.setProfilePictureUrl(profilePicUrl);
            logger.info("註冊用戶 {} 上傳頭像成功。URL: {}", requestDTO.getEmail(), profilePicUrl);
        }

        UserModel savedUser = userRepository.save(user);
        logger.info("已為用戶 {} 生成驗證碼並準備發送郵件。", user.getEmail());
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
     * 更新用戶個人資料 (包含名稱、YouTube 和 Instagram 連結及頭像)
     * @param userId 要更新的用戶 ID
     * @param updateDTO 包含更新資訊的 DTO
     * @return 更新後的 UserModel
     */
    @Override
    @Transactional
    public UserModel updateProfile(Integer userId, UserProfileUpdateDTO updateDTO) throws IOException { // 增加 IOException
        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("未找到用戶 ID: " + userId));

        user.setName(updateDTO.getName());
        user.setYtUrl(updateDTO.getYtUrl());
        user.setIgUrl(updateDTO.getIgUrl());

        // 處理頭像
        MultipartFile profilePictureFile = updateDTO.getProfilePictureFile();
        String oldProfilePictureUrl = user.getProfilePictureUrl();

        if (profilePictureFile != null && !profilePictureFile.isEmpty()) {
            // 上傳新圖片
            String newProfilePictureUrl = gcsService.uploadFile(profilePictureFile, "profile-pictures/");
            user.setProfilePictureUrl(newProfilePictureUrl);
            logger.info("用戶 ID: {} 更新頭像成功。新URL: {}", userId, newProfilePictureUrl);

            // 刪除舊圖片 (如果存在)
            if (oldProfilePictureUrl != null && !oldProfilePictureUrl.isEmpty()) {
                try {
                    gcsService.deleteFile(oldProfilePictureUrl);
                    logger.info("用戶 ID: {} 成功刪除舊頭像: {}", userId, oldProfilePictureUrl);
                } catch (Exception e) {
                    logger.error("用戶 ID: {} 刪除舊頭像失敗: {}. URL: {}", userId, e.getMessage(), oldProfilePictureUrl, e);
                }
            }
        } else if (profilePictureFile != null && profilePictureFile.isEmpty() && oldProfilePictureUrl != null) {
            // 如果 profilePictureFile 不為 null 但為空 (表示前端明確清空了文件選擇器)，並且有舊頭像，表示用戶希望刪除頭像而不上傳新頭像
            try {
                gcsService.deleteFile(oldProfilePictureUrl);
                user.setProfilePictureUrl(null); // 將 URL 設置為 null
                logger.info("用戶 ID: {} 成功刪除頭像 (無新頭像上傳): {}", userId, oldProfilePictureUrl);
            } catch (Exception e) {
                logger.error("用戶 ID: {} 刪除頭像失敗 (無新頭像上傳): {}. URL: {}", userId, e.getMessage(), oldProfilePictureUrl, e);
                throw new IOException("刪除頭像失敗", e);
            }
        }
        return userRepository.save(user);
    }

    @Override
    public UserProfileResponseDTO getUserProfileForPublicRecommend(Integer userId) {
        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("未找到用戶 ID: " + userId));

        UserProfileResponseDTO dto = new UserProfileResponseDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail()); // 公開推薦頁面可能不需要 email
        dto.setName(user.getName());
        dto.setYtUrl(user.getYtUrl());
        dto.setIgUrl(user.getIgUrl());
        dto.setProfilePictureUrl(user.getProfilePictureUrl());
        return dto;
    }
}