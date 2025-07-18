package com.example.topfoodnow.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileUpdateDTO {
    @Size(min = 2, max = 50, message = "用戶名長度必須在2到50個字符之間")
    private String name;
    private Boolean isFamous;
}