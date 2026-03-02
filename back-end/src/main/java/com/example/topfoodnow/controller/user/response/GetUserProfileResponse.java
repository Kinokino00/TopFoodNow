package com.example.topfoodnow.controller.user.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "獲取用戶檔案的響應對象")
public class GetUserProfileResponse {
    @Schema(description = "用戶ID", example = "1")
    private Integer id;

    @Schema(description = "用戶電子郵件", example = "john@example.com")
    private String email;

    @Schema(description = "用戶名稱", example = "John Doe")
    private String name;

    @Schema(description = "用戶YouTube URL", example = "https://youtube.com/channel/...")
    private String ytUrl;

    @Schema(description = "用戶Instagram URL", example = "https://instagram.com/...")
    private String igUrl;

    @Schema(description = "用戶頭像URL", example = "https://example.com/avatar.jpg")
    private String profilePictureUrl;
}
