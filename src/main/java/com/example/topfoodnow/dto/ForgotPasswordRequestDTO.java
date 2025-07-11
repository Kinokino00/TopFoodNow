package com.example.topfoodnow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "忘記密碼請求數據")
public class ForgotPasswordRequestDTO {
    @Schema(description = "用戶電子郵件", example = "user@example.com", required = true)
    private String email;
}