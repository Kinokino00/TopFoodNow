package com.example.topfoodnow.service;

import com.example.topfoodnow.model.UserModel;
import com.example.topfoodnow.dto.UserRegisterRequestDTO;
import com.example.topfoodnow.dto.UserProfileUpdateDTO;
import com.example.topfoodnow.dto.UserProfileResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Optional;

@Service
public interface UserService {
    Optional<UserModel> findById(Integer id);
    Optional<UserModel> findByEmail(String email);
    UserModel save(UserModel user);
    UserModel addUser(UserRegisterRequestDTO requestDTO) throws IOException;

    UserModel authenticate(String email, String password);
    boolean verifyAccount(String verificationCode);
    void createPasswordResetTokenForUser(String email);
    String validatePasswordResetToken(String token);
    void changeUserPassword(String token, String newPassword);

    UserModel updateProfile(Integer userId, UserProfileUpdateDTO updateDTO) throws IOException;
    UserProfileResponseDTO getUserProfileForPublicRecommend(Integer userId);
}