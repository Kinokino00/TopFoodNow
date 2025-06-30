package com.example.topfoodnow.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user")
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class UserModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(unique = true, nullable = false)
    @NonNull
    @Email(message = "信箱格式錯誤")
    @NotBlank(message = "信箱不可為空")
    private String email;

    @Column(nullable = false)
    @NonNull
    @NotBlank(message = "密碼不可為空")
    @Size(min = 8, message = "密碼不可少於8位")
    private String password;

    @Column(nullable = false)
    @NonNull
    @NotBlank(message = "名稱不可為空")
    private String userName;

    @Column(name = "is_famous", nullable = false, columnDefinition = "BIT(1) DEFAULT 0")
    @NonNull
    private boolean isFamous;

    @Column(nullable = false)
    @NonNull
    private boolean enabled;

    @Column(name = "verification_code", length = 64, unique = true)
    private String verificationCode;
    @Column(name = "reset_password_token")
    private String resetPasswordToken;

    @Column(name = "reset_password_expiry_date")
    private LocalDateTime resetPasswordExpiryDate;

    public boolean isResetPasswordTokenExpired() {
        return this.resetPasswordExpiryDate != null && LocalDateTime.now().isAfter(this.resetPasswordExpiryDate);
    }
}
