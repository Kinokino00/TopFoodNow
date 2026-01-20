package com.example.topfoodnow.service.user;

import com.example.topfoodnow.infra.user.User;
import com.example.topfoodnow.controller.user.request.UserRegisterRequest;
import com.example.topfoodnow.controller.user.request.UpdateUserProfileRequest;
import com.example.topfoodnow.controller.user.response.UserProfileResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
public interface UserService {
    Optional<User> findById(Integer id);
    Optional<User> findByEmail(String email);
    User save(User user);
    User addUser(UserRegisterRequest requestDTO) throws IOException;

    User authenticate(String email, String password);
    boolean verifyAccount(String verificationCode);
    void createPasswordResetTokenForUser(String email);
    String validatePasswordResetToken(String token);
    void changeUserPassword(String token, String newPassword);

    User updateProfile(Integer userId, UpdateUserProfileRequest updateDTO) throws IOException;
    UserProfileResponse getUserProfileForPublicRecommend(Integer userId);
}