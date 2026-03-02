package com.example.topfoodnow.controller.filestorage.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadFileResponse {
  private String fileUrl;
  private String message;
  private Boolean success;
}
