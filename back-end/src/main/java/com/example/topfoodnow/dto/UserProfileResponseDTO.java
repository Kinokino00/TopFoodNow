package com.example.topfoodnow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用戶資料響應數據")
public class UserProfileResponseDTO {
    @Schema(description = "用戶ID")
    private Integer id;

    @Schema(description = "用戶電子郵件")
    private String email;

    @Schema(description = "用戶名稱")
    private String name;

    @Schema(description = "YouTube 頻道連結", nullable = true)
    private String ytUrl;

    @Schema(description = "Instagram 個人檔案連結", nullable = true)
    private String igUrl;

    @Schema(description = "頭像", nullable = true)
    private String profilePictureUrl;
}