package com.example.topfoodnow.controller.filestorage;

import com.example.topfoodnow.controller.filestorage.request.DeleteFileRequest;
import com.example.topfoodnow.controller.filestorage.request.UploadFileRequest;
import com.example.topfoodnow.controller.filestorage.response.DeleteFileResponse;
import com.example.topfoodnow.controller.filestorage.response.UploadFileResponse;
import com.example.topfoodnow.service.filestorage.impl.DeleteFileUseCase;
import com.example.topfoodnow.service.filestorage.impl.UploadFileUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/frontend")
@Tag(name = "文件管理")
public class FileStorageController {

  private final UploadFileUseCase uploadFileUseCase;
  private final DeleteFileUseCase deleteFileUseCase;

  @Operation(summary = "上傳文件")
  @PostMapping(value = "/uploadFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<UploadFileResponse> uploadFile(
      @ModelAttribute @Valid UploadFileRequest rq) {
    return ResponseEntity.ok(uploadFileUseCase.execute(rq));
  }

  @Operation(summary = "刪除文件")
  @PostMapping("/deleteFile")
  public ResponseEntity<DeleteFileResponse> deleteFile(
      @RequestBody @Valid DeleteFileRequest rq) {
    return ResponseEntity.ok(deleteFileUseCase.execute(rq));
  }
}
