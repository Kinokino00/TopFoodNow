package com.example.topfoodnow.controller.user.response;

import com.example.topfoodnow.infra.user.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "認證響應數據，通常包含JWT Token和用戶信息")
public class RoleResponse {
    @Schema(description = "操作是否成功", example = "true")
    private boolean success;

    @Schema(description = "響應信息", example = "登入成功")
    private String message;

    @Schema(description = "JWT 認證 Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
    private String token;

    @Schema(description = "用戶詳細資訊", implementation = User.class)
    private User user;
}