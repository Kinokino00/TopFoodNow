package com.example.topfoodnow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data // 自動生成 getter/setter、toString、equals、hashCode
@NoArgsConstructor  // 自動生成無參數建構子
@AllArgsConstructor // 自動生成全參數建構子
@Schema(description = "忘記密碼請求數據")
public class ForgotPasswordRequestDTO {
    @Schema(description = "用戶電子郵件", example = "user@example.com", required = true)
    private String email;
}