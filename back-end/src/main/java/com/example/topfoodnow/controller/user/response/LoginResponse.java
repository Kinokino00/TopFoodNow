package com.example.topfoodnow.controller.user.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登入響應對象，返回用戶的角色信息")
public class LoginResponse {
    @Schema(description = "用戶角色ID", example = "1")
    private Integer roleId;

    @Schema(description = "用戶角色名稱", example = "USER")
    private String roleName;

    @Schema(description = "JWT 令牌", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;
}
