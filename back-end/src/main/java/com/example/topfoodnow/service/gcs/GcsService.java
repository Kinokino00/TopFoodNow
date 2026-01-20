package com.example.topfoodnow.service.gcs;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface GcsService {
    String uploadFile(MultipartFile file, String folderPath) throws IOException;
    void deleteFile(String fileUrl);
}