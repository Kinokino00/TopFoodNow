package com.example.topfoodnow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用戶登入請求數據")
public class LoginRequestDTO {
    @NotBlank(message = "信箱不能為空")
    @Email(message = "信箱格式不正確")
    @Schema(description = "用戶電子郵件", example = "user@example.com")
    private String email;

    @NotBlank(message = "密碼不能為空")
    @Schema(description = "用戶密碼", example = "yourpassword")
    private String password;
}