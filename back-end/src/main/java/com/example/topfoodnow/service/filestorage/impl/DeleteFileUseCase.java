package com.example.topfoodnow.service.filestorage.impl;

import com.example.topfoodnow.controller.filestorage.request.DeleteFileRequest;
import com.example.topfoodnow.controller.filestorage.response.DeleteFileResponse;
import com.example.topfoodnow.service.filestorage.FileStorageService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class DeleteFileUseCase {

  private final FileStorageService fileStorageService;

  public DeleteFileResponse execute(DeleteFileRequest rq) {
    log.info(">>>> DeleteFileUseCase : Request = {}", rq);

    try {
      fileStorageService.deleteFile(rq.getFileUrl());

      DeleteFileResponse response = DeleteFileResponse.builder()
              .success(true)
              .message("File deleted successfully")
              .build();

      log.info(">>>> DeleteFileUseCase : Response = {}", response);
      return response;
    } catch (Exception e) {
      log.error(">>>> DeleteFileUseCase : Error deleting file: {}", e.getMessage(), e);
      return DeleteFileResponse.builder()
              .success(false)
              .message("Error deleting file: " + e.getMessage())
              .build();
    }
  }
}
