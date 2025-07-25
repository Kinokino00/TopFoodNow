package com.example.topfoodnow.service;

import com.example.topfoodnow.model.UserModel;
import com.example.topfoodnow.dto.UserProfileUpdateDTO;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public interface UserService {
    Optional<UserModel> findById(Integer id);
    Optional<UserModel> findByEmail(String email);
    UserModel addUser(UserModel user);
    UserModel authenticate(String email, String password);
    boolean verifyAccount(String verificationCode);
    void createPasswordResetTokenForUser(String email);
    String validatePasswordResetToken(String token);
    void changeUserPassword(String token, String newPassword);
    UserModel updateProfile(Integer userId, UserProfileUpdateDTO updateDTO);
}