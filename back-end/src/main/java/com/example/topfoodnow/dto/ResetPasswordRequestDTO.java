package com.example.topfoodnow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank; // 確保導入
import jakarta.validation.constraints.Size; // 確保導入

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "重設密碼請求數據")
public class ResetPasswordRequestDTO {
    @NotBlank(message = "Token不能為空")
    @Schema(description = "密碼重設Token", example = "a1b2c3d4e5f6", required = true)
    private String token;

    @NotBlank(message = "新密碼不能為空")
    @Size(min = 8, message = "密碼長度至少為 8 個字符")
    @Schema(description = "新密碼 (至少8位)", example = "newPass123", required = true)
    private String newPassword;

    @NotBlank(message = "確認密碼不能為空")
    @Schema(description = "確認新密碼", example = "newPass123", required = true)
    private String confirmPassword;
}