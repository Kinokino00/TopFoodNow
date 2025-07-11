package com.example.topfoodnow.util;

import com.example.topfoodnow.model.AiRestaurantModel;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.io.FileUtils;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.concurrent.Callable;
import java.util.UUID;

public class ScreenShotTask implements Callable<AiRestaurantModel> {
    private static final Logger logger = LoggerFactory.getLogger(ScreenShotTask.class);
    private final AiRestaurantModel restaurant;
    private final Path screenshotSaveLocation;
    private final String screenshotSubDir;

    public ScreenShotTask(AiRestaurantModel restaurant, Path screenshotSaveLocation, String screenshotSubDir) {
        this.restaurant = restaurant;
        this.screenshotSaveLocation = screenshotSaveLocation;
        this.screenshotSubDir = screenshotSubDir;
    }

    @Override
    public AiRestaurantModel call() throws Exception {
        WebDriver driver = null;
        try {
            if (restaurant.getUrl() == null || restaurant.getUrl().trim().isEmpty()) {
                logger.warn("餐廳: {} 的 URL 為空或無效 (URL: '{}')，跳過截圖。",
                        restaurant.getName(), (restaurant.getUrl() == null ? "null" : "'" + restaurant.getUrl() + "'"));
                restaurant.setPhotoUrl("https://via.placeholder.com/150?text=No+URL");
                return restaurant;
            }

            WebDriverManager.chromedriver().setup();

            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");
            options.addArguments("--disable-gpu");
            options.addArguments("--no-sandbox");
            options.addArguments("--window-size=1280,800");

            driver = new ChromeDriver(options);

            logger.debug("開始截圖: {}", restaurant.getUrl());
            driver.get(restaurant.getUrl());

            String fileName = UUID.randomUUID().toString() + ".png";
            Path filePath = screenshotSaveLocation.resolve(fileName);

            Files.createDirectories(filePath.getParent());

            File screenshotFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(screenshotFile, filePath.toFile());

            String photoUrl = "/dynamic-content/" + screenshotSubDir + "/" + fileName;
            photoUrl = photoUrl.replace("\\", "/");

            restaurant.setPhotoUrl(photoUrl);
            logger.info("成功截圖並保存: {} -> {}", restaurant.getUrl(), restaurant.getPhotoUrl());

            return restaurant;
        } catch (Exception e) {
            logger.error("截圖失敗: {} (URL: {}), 錯誤: {}",
                    restaurant.getName(), restaurant.getUrl() != null ? restaurant.getUrl() : "URL為空", e.getMessage(), e);
            restaurant.setPhotoUrl("https://via.placeholder.com/150?text=Error+Image");
            return restaurant;
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }
}