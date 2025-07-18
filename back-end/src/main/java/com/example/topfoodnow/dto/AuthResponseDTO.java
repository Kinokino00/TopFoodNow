package com.example.topfoodnow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "認證響應數據，通常包含JWT Token和用戶信息")
public class AuthResponseDTO {
    @Schema(description = "操作是否成功", example = "true")
    private boolean success;

    @Schema(description = "響應信息", example = "登入成功。")
    private String message;

    @Schema(description = "JWT 認證 Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
    private String token;

    @Schema(description = "用戶電子郵件", example = "user@example.com")
    private String userEmail;
}