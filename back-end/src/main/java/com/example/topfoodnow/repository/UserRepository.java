package com.example.topfoodnow.repository;

import com.example.topfoodnow.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserModel, Integer> {
    Optional<UserModel> findByEmail(String email);
    Optional<UserModel> findByVerificationCode(String verificationCode);
    Optional<UserModel> findByResetPasswordToken(String resetPasswordToken);
}
