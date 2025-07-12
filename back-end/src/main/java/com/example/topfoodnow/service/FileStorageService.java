package com.example.topfoodnow.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {
    @Value("${file.dynamic-content-base-dir}")
    private String dynamicContentBaseDir;

    @Value("${file.upload-sub-dir}")
    private String uploadSubDir;

    private Path fileUploadLocation;

    public FileStorageService(@Value("${file.dynamic-content-base-dir}") String baseDir,
                              @Value("${file.upload-sub-dir}") String uploadDir) {
        this.fileUploadLocation = Paths.get(baseDir, uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileUploadLocation);
        } catch (IOException ex) {
            throw new RuntimeException("無法創建文件上傳目錄！" + this.fileUploadLocation, ex);
        }
    }

    /**
     * 上傳文件並返回其在應用程式中的相對 URL 路徑
     * @param file 要上傳的 MultipartFile
     * @return 文件的相對 URL 路徑
     * @throws IOException 如果文件儲存失敗
     */
    public String uploadFile(MultipartFile file) throws IOException {
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileName = UUID.randomUUID().toString() + "_" + originalFileName;

        Path targetLocation = this.fileUploadLocation.resolve(fileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        return "/dynamic-content/" + uploadSubDir + fileName;
    }
}