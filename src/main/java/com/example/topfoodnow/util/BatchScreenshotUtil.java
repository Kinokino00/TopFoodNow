package com.example.topfoodnow.util;

import com.example.topfoodnow.model.AiRestaurantModel;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

@Component
public class BatchScreenshotUtil {
    public List<AiRestaurantModel> captureScreenshots(List<AiRestaurantModel> list, String screenshotDir) {
        return captureScreenshots(list, screenshotDir, null);
    }

    public List<AiRestaurantModel> captureScreenshots(
            List<AiRestaurantModel> list,
            String screenshotDir,
            BiConsumer<AiRestaurantModel, Integer> onSuccess
    ) {
        List<AiRestaurantModel> successList = new ArrayList<>();
        File dir = new File(screenshotDir);
        if (!dir.exists()) dir.mkdirs();

        try {
            for (File file : dir.listFiles()) {
                file.delete();
            }
        } catch (Exception ignored) {}

        WebDriver driver = null;
        try {
            WebDriverManager.chromedriver().setup();

            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless=new");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1280,800");
            options.addArguments("--no-sandbox");
            options.addArguments("--ignore-certificate-errors");
            options.addArguments("--log-level=3");

            ChromeDriverService service = new ChromeDriverService.Builder()
                    .withSilent(true)
                    .build();

            driver = new ChromeDriver(service, options);

            int count = 0;
            for (AiRestaurantModel info : list) {
                if (count >= 6) break;
                String url = info.getUrl();
                if (isBlacklistedUrl(url)) continue;

                boolean success = false;
                int retries = 0;
                while (!success && retries < 3) {
                    try {
                        driver.get(url);
                        Thread.sleep(8000); // 增加等待時間

                        String pageTitle = driver.getTitle().toLowerCase();
                        String pageSource = driver.getPageSource().toLowerCase();

                        if (pageTitle.contains("404") || pageSource.contains("404")) break;
                        if (isSuspiciousPage(pageTitle, pageSource)) break;

                        File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                        File dest = new File(dir, "restaurant_" + (count + 1) + ".png");
                        Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);

                        info.setPhotoUrl("screenshots/" + dest.getName());
                        successList.add(info);
                        if (onSuccess != null) onSuccess.accept(info, count);
                        count++;
                        success = true;
                    } catch (Exception e) {
                        retries++;
                        if (retries >= 3) {
                            System.err.println("多次截圖失敗: " + url);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("BatchScreenshotUtil 發生致命錯誤，無法初始化 WebDriver: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
        return successList;
    }

    private boolean isUrlReachable(String url) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("HEAD");
            conn.setConnectTimeout(2000);
            conn.connect();
            return conn.getResponseCode() < 400;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isBlacklistedUrl(String url) {
        if (url == null || url.isBlank()) return true;
        String lower = url.toLowerCase();
        return lower.contains("facebook.com") ||
                lower.contains("instagram.com") ||
                lower.contains("gov.tw/login") ||
                lower.contains("expired-cert") ||
                lower.startsWith("http://") ||
                lower.endsWith(".info") ||
                !lower.startsWith("https://");
    }

    private static boolean isSuspiciousPage(String title, String html) {
        return title.contains("不安全") ||
                title.contains("error") ||
                title.contains("404") ||
                html.contains("net::err_cert") ||
                html.contains("connection is not private") ||
                html.contains("安全性憑證") ||
                html.contains("攻擊者可能正在");
    }
}