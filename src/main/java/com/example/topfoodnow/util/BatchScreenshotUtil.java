// src/main/java/com/example/topfoodnow/util/BatchScreenshotUtil.java

package com.example.topfoodnow.util;

import com.example.topfoodnow.model.AiRestaurantModel; // 確保路徑正確
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.time.Duration;

@Component
public class BatchScreenshotUtil {
    private static final Logger logger = LoggerFactory.getLogger(BatchScreenshotUtil.class);

    @Value("${file.dynamic-content-base-dir}")
    private String dynamicContentBaseDir;

    @Value("${file.screenshot-sub-dir}")
    private String screenshotSubDir;

    private Path screenshotSaveLocation;

    public BatchScreenshotUtil(@Value("${file.dynamic-content-base-dir}") String baseDir,
                               @Value("${file.screenshot-sub-dir}") String subDir) {
        this.screenshotSaveLocation = Paths.get(baseDir, subDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.screenshotSaveLocation);
            logger.info("截圖儲存目錄初始化成功: {}", this.screenshotSaveLocation);
        } catch (IOException ex) {
            logger.error("無法創建截圖儲存目錄！請檢查路徑和權限: {}", this.screenshotSaveLocation, ex);
            throw new RuntimeException("無法創建截圖儲存目錄！", ex);
        }
    }

    /**
     * 批量捕捉餐廳網址的截圖
     * @param aiRestaurantList 包含餐廳名稱和 URL 的列表
     * @return 成功截圖的 AiRestaurantModel 列表，其中包含截圖的 URL
     */
    public List<AiRestaurantModel> captureScreenshots(List<AiRestaurantModel> aiRestaurantList) {
        List<AiRestaurantModel> successfulScreenshots = new ArrayList<>();
        WebDriver driver = null;
        try {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");    // 無頭模式
            options.addArguments("--disable-gpu"); // 禁用 GPU 加速 (在無頭模式下可能不需要)
            options.addArguments("--no-sandbox");  // 禁用沙箱模式 (尤其在 Docker 環境中需要)
            options.addArguments("--disable-dev-shm-usage");     // 解決 /dev/shm 空間不足問題
            options.addArguments("--window-size=1280,800");      // 設置窗口大小
            options.addArguments("--ignore-certificate-errors"); // 忽略證書錯誤
            options.addArguments("--disable-extensions");        // 禁用擴展
            options.addArguments("--remote-allow-origins=*");    // 允許所有遠程來源，處理 CORS 問題

            // 初始化 ChromeDriver
            driver = new ChromeDriver(options);
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30)); // 頁面加載超時
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));  // 隱式等待

            for (AiRestaurantModel restaurant : aiRestaurantList) {
                String url = restaurant.getUrl();
                if (url == null || url.isEmpty()) {
                    logger.warn("餐廳 '{}' 沒有提供 URL，跳過截圖。", restaurant.getName());
                    continue;
                }

                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "http://" + url; // 預設使用 http
                    logger.warn("餐廳 '{}' 的 URL '{}' 缺少協議頭，已自動補齊為 '{}'。", restaurant.getName(), restaurant.getUrl(), url);
                }

                try {
                    logger.info("嘗試訪問 URL: {}", url);
                    driver.get(url);
                    String screenshotUrl = captureScreenshot(driver, restaurant.getName());
                    if (screenshotUrl != null) {
                        restaurant.setPhotoUrl(screenshotUrl);
                        successfulScreenshots.add(restaurant);
                        logger.info("成功截圖 '{}'，圖片 URL: {}", restaurant.getName(), screenshotUrl);
                    } else {
                        logger.warn("截圖 '{}' 失敗，URL: {}", restaurant.getName(), url);
                    }
                } catch (Exception e) {
                    logger.error("訪問或截圖 '{}' ({}) 時發生錯誤: {}", restaurant.getName(), url, e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.error("初始化 Selenium WebDriver 時發生錯誤: {}", e.getMessage(), e);
        } finally {
            if (driver != null) {
                driver.quit();
                logger.info("Selenium WebDriver 已關閉。");
            }
        }
        return successfulScreenshots;
    }

    /**
     * 捕捉單個頁面的截圖並保存到指定目錄
     * @param driver       WebDriver 實例
     * @param baseFilename 截圖的基礎文件名
     * @return 截圖的相對 URL 路徑，如果失敗則返回 null
     */
    private String captureScreenshot(WebDriver driver, String baseFilename) {
        try {
            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String cleanBaseFilename = baseFilename.replaceAll("[^a-zA-Z0-9.-]", "_");
            String targetFileName = UUID.randomUUID().toString() + "_" + cleanBaseFilename + ".png"; // 確保唯一性

            Path destPath = this.screenshotSaveLocation.resolve(targetFileName);
            Files.copy(srcFile.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);

            return "/dynamic-content/" + screenshotSubDir + targetFileName;
        } catch (IOException e) {
            logger.error("保存截圖失敗，文件名: {}. 錯誤: {}", baseFilename, e.getMessage(), e);
            return null;
        }
    }
}