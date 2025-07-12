package com.example.topfoodnow.util;

import com.example.topfoodnow.model.AiRestaurantModel;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
@Component
public class BatchScreenshotUtil {
    private static final Logger logger = LoggerFactory.getLogger(BatchScreenshotUtil.class);

    @Value("${file.dynamic-content-base-dir}")
    private String dynamicContentBaseDir;

    @Value("${file.screenshot-sub-dir}")
    private String screenshotSubDir;

    private Path screenshotSaveLocation;

    private final ExecutorService executorService;
    private static final int MAX_THREADS = 4;

    public BatchScreenshotUtil(@Value("${file.dynamic-content-base-dir}") String baseDir,
                               @Value("${file.screenshot-sub-dir}") String subDir) {
        this.screenshotSaveLocation = Paths.get(baseDir, subDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.screenshotSaveLocation);
            logger.info("截圖儲存目錄初始化成功: {}", this.screenshotSaveLocation);
            cleanScreenshotDirectory();
        } catch (IOException ex) {
            logger.error("無法創建截圖儲存目錄！請檢查路徑和權限: {}", this.screenshotSaveLocation, ex);
            throw new RuntimeException("無法創建截圖儲存目錄！", ex);
        }
        this.executorService = Executors.newFixedThreadPool(MAX_THREADS);
    }

    private void cleanScreenshotDirectory() {
        try {
            Files.list(this.screenshotSaveLocation)
                    .filter(p -> p.toString().toLowerCase().endsWith(".png"))
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                            logger.debug("已刪除舊截圖: {}", p.getFileName());
                        } catch (IOException e) {
                            logger.warn("無法刪除舊截圖 {}: {}", p.getFileName(), e.getMessage());
                        }
                    });
            logger.info("已清理截圖目錄: {}", this.screenshotSaveLocation);
        } catch (IOException e) {
            logger.error("清理截圖目錄 {} 時發生錯誤: {}", this.screenshotSaveLocation, e.getMessage());
        }
    }

    public List<AiRestaurantModel> captureScreenshots(List<AiRestaurantModel> aiRestaurantList) {
        logger.info("開始批量截圖，共 {} 個任務。", aiRestaurantList.size());
        List<Future<AiRestaurantModel>> futures = new ArrayList<>();
        List<AiRestaurantModel> processedRestaurants = new ArrayList<>(); // 用於收集所有處理過的餐廳，無論成功或失敗

        for (AiRestaurantModel restaurant : aiRestaurantList) {
            ScreenShotTask task = new ScreenShotTask(restaurant, screenshotSaveLocation, screenshotSubDir);
            futures.add(executorService.submit(task));
        }

        int successCount = 0;
        int skippedCount = 0;
        int errorCount = 0;

        for (Future<AiRestaurantModel> future : futures) {
            try {
                AiRestaurantModel resultRestaurant = future.get(5, TimeUnit.MINUTES);
                if (resultRestaurant != null) {
                    processedRestaurants.add(resultRestaurant);
                    if (resultRestaurant.getPhotoUrl() != null) {
                        if (resultRestaurant.getPhotoUrl().contains("No+URL")) {
                            skippedCount++;
                        } else if (resultRestaurant.getPhotoUrl().contains("Error+Image")) {
                            errorCount++;
                        } else {
                            successCount++;
                        }
                    } else {
                        logger.error("餐廳 {} 截圖結果的 photoUrl 為空，但未標記為跳過或錯誤。", resultRestaurant.getName());
                        errorCount++;
                    }
                }
            } catch (InterruptedException e) {
                logger.error("批量截圖中的任務被中斷: {}", e.getMessage(), e);
                Thread.currentThread().interrupt();
                errorCount++;
            } catch (ExecutionException e) {
                logger.error("批量截圖中的任務執行失敗 (ExecutionException): {}", e.getCause() != null ? e.getCause().getMessage() : e.getMessage(), e);
                errorCount++;
            } catch (Exception e) {
                logger.error("獲取截圖任務結果時發生錯誤 (其他異常): {}", e.getMessage(), e);
                errorCount++;
            }
        }

        logger.info("批量截圖完成。總任務數: {}，成功截圖: {} 個，因 URL 為空跳過: {} 個，截圖失敗: {} 個。",
                aiRestaurantList.size(), successCount, skippedCount, errorCount);
        return processedRestaurants;
    }

    @PreDestroy
    public void cleanup() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                    logger.warn("BatchScreenshotUtil 的線程池未能完全關閉在指定時間內，嘗試強制關閉。");
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                logger.warn("BatchScreenshotUtil 的線程池關閉時被中斷。", e);
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
            logger.info("BatchScreenshotUtil 的線程池已關閉。");
        }
    }
}