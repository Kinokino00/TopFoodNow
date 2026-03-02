package com.example.topfoodnow.service.filestorage.impl;

import com.example.topfoodnow.controller.filestorage.request.UploadFileRequest;
import com.example.topfoodnow.controller.filestorage.response.UploadFileResponse;
import com.example.topfoodnow.service.filestorage.FileStorageService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
@AllArgsConstructor
public class UploadFileUseCase {

  private final FileStorageService fileStorageService;

  public UploadFileResponse execute(UploadFileRequest rq) {
    log.info(">>>> UploadFileUseCase : Request = {}", rq);

    try {
      String fileUrl = fileStorageService.uploadFile(rq.getFile(), rq.getFolderPath());

      UploadFileResponse response = UploadFileResponse.builder()
              .fileUrl(fileUrl)
              .success(true)
              .message("File uploaded successfully")
              .build();

      log.info(">>>> UploadFileUseCase : Response = {}", response);
      return response;
    } catch (IOException e) {
      log.error(">>>> UploadFileUseCase : Error uploading file: {}", e.getMessage(), e);
      return UploadFileResponse.builder()
              .fileUrl(null)
              .success(false)
              .message("Error uploading file: " + e.getMessage())
              .build();
    }
  }
}
