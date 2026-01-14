package com.example.topfoodnow.service.user;

import com.example.topfoodnow.model.UserModel;
import java.util.Optional;

public interface UserService {
    Optional<UserModel> findById(Integer id);
    Optional<UserModel> findByEmail(String email);
    UserModel addUser(UserModel user);
    UserModel authenticate(String email, String password);
    boolean verifyAccount(String verificationCode);
    String createPasswordResetTokenForUser(UserModel user);
    Optional<UserModel> validatePasswordResetToken(String token);
    void changeUserPassword(UserModel user, String newPassword);
}
