package com.example.topfoodnow.controller.filestorage.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteFileRequest {
  @NotBlank(message = "fileUrl cannot be blank")
  private String fileUrl;
}
