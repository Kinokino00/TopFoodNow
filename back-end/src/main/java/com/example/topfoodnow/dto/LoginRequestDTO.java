package com.example.topfoodnow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用戶登入請求數據")
public class LoginRequestDTO {
    @Schema(description = "用戶電子郵件", example = "user@example.com")
    private String email;

    @Schema(description = "用戶密碼", example = "yourpassword")
    private String password;
}