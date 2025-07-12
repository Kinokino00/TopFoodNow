package com.example.topfoodnow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "重設密碼請求數據")
public class ResetPasswordRequestDTO {
    @Schema(description = "密碼重設Token", example = "a1b2c3d4e5f6", required = true)
    private String token;

    @Schema(description = "新密碼 (至少8位)", example = "newPass123", required = true)
    private String newPassword;

    @Schema(description = "確認新密碼", example = "newPass123", required = true)
    private String confirmPassword;
}