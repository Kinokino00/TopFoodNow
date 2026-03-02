package com.example.topfoodnow.controller.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用戶註冊請求對象")
public class RegisterRequest {
    @NotBlank(message = "用戶名稱不能為空")
    @Schema(description = "用戶名稱", example = "John Doe")
    private String name;

    @NotBlank(message = "電子郵件不能為空")
    @Email(message = "電子郵件格式不正確")
    @Schema(description = "用戶的電子郵件地址", example = "john@example.com")
    private String email;

    @NotBlank(message = "密碼不能為空")
    @Schema(description = "用戶密碼", example = "password123")
    private String password;

    @Schema(description = "YouTube URL", example = "https://youtube.com/channel/...")
    private String ytUrl;

    @Schema(description = "Instagram URL", example = "https://instagram.com/...")
    private String igUrl;

    @Schema(description = "用戶頭像文件", example = "profile-picture.jpg")
    private MultipartFile profilePictureFile;
}
