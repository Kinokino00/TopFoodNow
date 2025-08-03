package com.example.topfoodnow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "自定義分頁響應 DTO")
public class CustomPageResponseDTO<T> {
    @Schema(description = "分頁內容數據列表")
    private List<T> data;

    @Schema(description = "分頁信息")
    private CustomPageableInfo pageable;

    @Schema(description = "總元素數量")
    private long totalElements;

    @Schema(description = "總頁數")
    private int totalPages;

    @Data
    @Schema(description = "分頁")
    public static class CustomPageableInfo {
        @Schema(description = "當前頁碼", example = "1")
        private int pageNumber;

        @Schema(description = "每頁大小", example = "10")
        private int pageSize;

        @Schema(description = "排序欄位名稱", example = "id")
        private String sortBy;

        @Schema(description = "排序方式 (ASC/DESC)", example = "ASC")
        private String sortOrder;
    }
}