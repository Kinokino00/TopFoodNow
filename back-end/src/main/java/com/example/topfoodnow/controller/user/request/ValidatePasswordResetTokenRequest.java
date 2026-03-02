package com.example.topfoodnow.controller.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "忘記密碼請求對象")
public class ValidatePasswordResetTokenRequest {
    @NotBlank(message = "重設密碼令牌不能為空")
    @Schema(description = "重設密碼令牌", example = "abc123xyz...")
    private String token;
}
