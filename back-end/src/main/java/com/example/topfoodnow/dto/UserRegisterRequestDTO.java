package com.example.topfoodnow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "用戶註冊請求數據")
public class UserRegisterRequestDTO {
    @NotBlank(message = "信箱不可為空")
    @Email(message = "信箱格式錯誤")
    @Schema(description = "用戶電子郵件", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "密碼不可為空")
    @Size(min = 8, message = "密碼不可少於8位")
    @Schema(description = "用戶密碼", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @NotBlank(message = "名稱不可為空")
    @Schema(description = "用戶名稱", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "YouTube 頻道連結", nullable = true)
    private String ytUrl;

    @Schema(description = "Instagram 個人檔案連結", nullable = true)
    private String igUrl;

    @Schema(description = "頭像", type = "string", format = "binary", nullable = true)
    private MultipartFile profilePictureFile;
}