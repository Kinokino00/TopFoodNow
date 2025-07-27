package com.example.topfoodnow.service.impl;

import com.example.topfoodnow.service.FileStorageService;
import com.example.topfoodnow.service.GcsService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {
    private final GcsService gcsService;

    /**
     * 上傳文件到 GCS
     * @param file 要上傳的 MultipartFile
     * @param folderPath 在 GCS 儲存桶中的目標資料夾路徑 (例如 "store-images/")
     * @return 文件的 GCS URL
     * @throws IOException 如果文件儲存失敗 (GcsService 會處理)
     */
    public String uploadFile(MultipartFile file, String folderPath) throws IOException {
        return gcsService.uploadFile(file, folderPath);
    }

    /**
     * 從 GCS 刪除文件
     * @param fileUrl 要刪除的文件的完整 GCS URL
     */
    public void deleteFile(String fileUrl) {
        gcsService.deleteFile(fileUrl);
    }
}