package com.example.topfoodnow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "更新用戶資料請求數據")
public class UserProfileUpdateDTO {
    @NotBlank(message = "名稱不可為空")
    @Schema(description = "用戶名稱")
    private String name;

    @Schema(description = "YouTube 頻道連結", nullable = true)
    private String ytUrl;

    @Schema(description = "Instagram 個人檔案連結", nullable = true)
    private String igUrl;

    // 頭像(如果為 null 或空，表示不更新頭像)
    @Schema(description = "頭像", type = "string", format = "binary", nullable = true)
    private MultipartFile profilePictureFile;
}