package com.example.topfoodnow.dto;

import java.util.List;
import lombok.Data;

@Data
public class CustomPageResponseDTO<T> {
    private List<T> content;
    private CustomPageableInfo pageable;
    private long totalElements;
    private int totalPages;

    @Data
    public static class CustomPageableInfo {
        private int pageNumber;
        private int pageSize;
        private String sort;
    }
}