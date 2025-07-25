package com.example.topfoodnow.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用戶資料更新請求對象")
public class UserProfileUpdateDTO {
    @NotBlank(message = "用戶名稱不可為空")
    @Schema(description = "用戶名稱", example = "新的用戶名")
    private String name;

    @Schema(description = "YouTube 頻道連結", example = "https://www.youtube.com/channel/new_channel", nullable = true)
    private String ytUrl;

    @Schema(description = "Instagram 個人檔案連結", example = "https://www.instagram.com/new_profile/", nullable = true)
    private String igUrl;
}