package com.example.topfoodnow.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    // 設定圖片儲存的基礎目錄
    // 這裡使用本地檔案系統作為示例。實際生產環境應考慮使用雲儲存服務 (如 AWS S3, Google Cloud Storage, Cloudinary 等)
    // 該目錄將在專案根目錄下創建一個 'uploads' 資料夾
    private final Path fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();

    public FileStorageService() {
        try {
            // 檢查並創建儲存目錄，如果它不存在的話
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            // 如果無法創建目錄，則拋出運行時異常
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    /**
     * 儲存上傳的檔案到本地檔案系統，並返回其相對 URL。
     * @param file 用戶上傳的 MultipartFile 對象
     * @return 儲存後檔案的相對 URL (例如: /uploads/your-generated-filename.png)
     * @throws RuntimeException 如果檔案儲存失敗
     */
    public String uploadFile(MultipartFile file) {
        // 獲取原始檔名，用於提取副檔名
        String originalFileName = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        // 生成一個唯一的檔名，以避免檔名衝突
        String fileName = UUID.randomUUID().toString() + fileExtension;

        try {
            // 檢查檔案是否為空
            if (file.isEmpty()) {
                throw new IOException("Failed to store empty file " + fileName);
            }

            // 解析目標路徑
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            // 將檔案的輸入流複製到目標位置，如果目標檔案已存在則覆蓋
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // 返回圖片的 URL。
            // 為了讓這個 URL 能被 Spring Boot 伺服器提供，
            // 您需要在 application.properties 中配置靜態資源映射：
            // spring.web.resources.static-locations=classpath:/static/,file:./uploads/
            // 或者：
            // spring.web.resources.static-locations[0]=classpath:/static/
            // spring.web.resources.static-locations[1]=file:./uploads/
            // 這樣，當瀏覽器請求 /uploads/filename 時，Spring Boot 會從 ./uploads/ 目錄下查找檔案。
            return "/uploads/" + fileName;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    // 您可以添加其他方法，例如下載檔案、刪除檔案等
    public void deleteFile(String filePath) {
        // 假設 filePath 是 uploadFile 返回的 /uploads/filename 格式
        if (filePath != null && filePath.startsWith("/uploads/")) {
            String fileName = filePath.substring("/uploads/".length());
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            try {
                Files.deleteIfExists(targetLocation);
            } catch (IOException e) {
                // 記錄錯誤，但可能不需要拋出，因為刪除失敗不應阻止主要業務流程
                System.err.println("Failed to delete file: " + fileName + " - " + e.getMessage());
            }
        }
    }
}