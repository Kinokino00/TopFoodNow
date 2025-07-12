package com.example.topfoodnow.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "user")
@Data
@NoArgsConstructor
@RequiredArgsConstructor
@Schema(description = "用戶資料")
public class UserModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "用戶ID", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private int id;

    @Column(unique = true, nullable = false)
    @NonNull
    @Email(message = "信箱格式錯誤")
    @NotBlank(message = "信箱不可為空")
    @Schema(description = "用戶電子郵件", example = "test@example.com")
    private String email;

    @Column(nullable = false)
    @NonNull
    @NotBlank(message = "密碼不可為空")
    @Size(min = 8, message = "密碼不可少於8位")
    @Schema(description = "用戶密碼", example = "password123")
    private String password;

    @Column(nullable = false)
    @NonNull
    @NotBlank(message = "名稱不可為空")
    @Schema(description = "用戶名稱", example = "John Doe")
    private String userName;

    @Column(name = "is_famous", nullable = false, columnDefinition = "BIT(1) DEFAULT 0")
    @Schema(description = "是否為網紅", example = "false")
    private Boolean isFamous;

    @Column(nullable = false)
    @NonNull
    @Schema(description = "帳戶是否啟用", example = "true", accessMode = Schema.AccessMode.READ_ONLY)
    private boolean enabled;

    @Column(name = "verification_code", length = 64, unique = true)
    @Schema(description = "帳戶驗證碼", accessMode = Schema.AccessMode.READ_ONLY, nullable = true)
    private String verificationCode;

    @Column(name = "reset_password_token")
    @Schema(description = "重設密碼Token", accessMode = Schema.AccessMode.READ_ONLY, nullable = true)
    private String resetPasswordToken;

    @Column(name = "reset_password_expiry_date")
    @Schema(description = "重設密碼Token過期時間", example = "2025-07-15T10:00:00", accessMode = Schema.AccessMode.READ_ONLY, nullable = true)
    private LocalDateTime resetPasswordExpiryDate;

    public boolean isResetPasswordTokenExpired() {
        return this.resetPasswordExpiryDate != null && LocalDateTime.now().isAfter(this.resetPasswordExpiryDate);
    }
}
