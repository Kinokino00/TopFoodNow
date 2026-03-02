package com.example.topfoodnow.controller.filestorage.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadFileRequest {
  @NotEmpty(message = "file cannot be empty")
  private MultipartFile file;

  @NotBlank(message = "folderPath cannot be blank")
  private String folderPath;
}
