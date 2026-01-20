package com.example.topfoodnow.service.filestorage;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface FileStorageService {
    String uploadFile(MultipartFile file, String folderPath) throws IOException;
    void deleteFile(String fileUrl);
}