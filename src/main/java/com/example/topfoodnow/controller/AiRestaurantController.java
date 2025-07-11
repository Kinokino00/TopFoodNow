package com.example.topfoodnow.controller;

import com.example.topfoodnow.model.AiRestaurantModel;
import com.example.topfoodnow.util.BatchScreenshotUtil;
import com.example.topfoodnow.util.ChatGPTUtil;
import com.example.topfoodnow.util.JsonUtil;
import com.example.topfoodnow.dto.RecommendDTO;
import com.example.topfoodnow.service.RecommendService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONObject;
import org.json.JSONException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/ai")
public class AiRestaurantController {
    private static final Logger logger = LoggerFactory.getLogger(AiRestaurantController.class);
    private final JsonUtil jsonUtil;
    private final BatchScreenshotUtil batchScreenshotUtil;
    private final RecommendService recommendService;

    @Value("${openai.api.key}")
    private String apiKey;

    public AiRestaurantController(JsonUtil jsonUtil,
                                  BatchScreenshotUtil batchScreenshotUtil,
                                  RecommendService recommendService) {
        this.jsonUtil = jsonUtil;
        this.batchScreenshotUtil = batchScreenshotUtil;
        this.recommendService = recommendService;
    }

    private static final String PROMPT =
            "請輸出台北6家熱門美食餐廳，包含名稱、地址和網址(盡量提供穩定可訪問的非社群媒體官方網站)。使用 JSON 陣列格式回傳，例如：\n" +
                    "[{\"name\": \"鼎泰豐\", \"address\": \"台北市信義路二段22號\", \"url\": \"https://www.dintaifung.com.tw/\"}]";

    @Operation(
        summary = "獲取 AI 推薦餐廳列表",
        description = "呼叫 OpenAI API 生成台北6家熱門美食餐廳推薦，並嘗試進行截圖。如果 AI 回傳解析失敗或截圖失敗，則從資料庫中提供備用餐廳列表。",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "成功獲取 AI 推薦或資料庫備用餐廳列表",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AiRestaurantModel.class, type = "array")
                )
            ),
            @ApiResponse(
                responseCode = "500",
                description = "伺服器內部錯誤，例如與 OpenAI API 通訊失敗或截圖服務異常",
                content = @Content(schema = @Schema(implementation = String.class))
            )
        }
    )
    @GetMapping
    public List<AiRestaurantModel> getAIRecommend() throws IOException {
        logger.info("啟動 AI 搜尋");

        String gptFullResponseJson = ChatGPTUtil.askGpt(PROMPT, apiKey);
        logger.debug("GPT 回傳原始 JSON: {}", gptFullResponseJson);

        String gptContent = null;
        try {
            JSONObject fullGptResponse = new JSONObject(gptFullResponseJson);
            gptContent = fullGptResponse
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");
            logger.debug("GPT message.content 原始內容 (已由 JSONObject 處理標準跳脫): {}", gptContent);
            logger.debug("GPT message.content 原始內容 (HEX): {}", toHex(gptContent));
        } catch (JSONException e) {
            logger.error("無法解析 GPT 原始回傳 JSON 或提取 'content' 欄位。", e);
            logger.error("GPT 原始回傳 JSON (HEX): {}", toHex(gptFullResponseJson));
            return getFallbackFromDatabase();
        }

        String extractedJsonString = null;
        Pattern pattern = Pattern.compile("```json\\s*\\n([\\s\\S]*?)\\n```");
        Matcher matcher = pattern.matcher(gptContent);

        if (matcher.find()) {
            extractedJsonString = matcher.group(1);
            logger.debug("從 Markdown 區塊中提取的 JSON 字串 (初步): {}", extractedJsonString);
            logger.debug("從 Markdown 區塊中提取的 JSON 字串 (初步 HEX): {}", toHex(extractedJsonString));

            extractedJsonString = extractedJsonString.replaceAll("[\\p{C}&&[^\t\n\r]]", "");
            extractedJsonString = extractedJsonString.trim();
        } else {
            logger.error("無法從 GPT 回應的 'content' 中提取有效的 JSON 程式碼區塊。GPT content: {}", gptContent);
            logger.error("GPT content (HEX): {}", toHex(gptContent));
            return getFallbackFromDatabase();
        }

        List<AiRestaurantModel> aiList;
        try {
            logger.debug("GPT 回傳最終清理後 JSON (傳給 JsonUtil 的內容): {}", extractedJsonString);
            logger.debug("GPT 回傳最終清理後 JSON (HEX): {}", toHex(extractedJsonString));

            aiList = jsonUtil.parseRestaurants(extractedJsonString);
        } catch (JSONException e) {
            logger.error("解析 GPT 回傳 JSON 失敗，傳入 JsonUtil 的內容有誤。內容: '{}'", extractedJsonString, e);
            logger.error("傳入 JsonUtil 的內容 (HEX): {}", toHex(extractedJsonString));
            return getFallbackFromDatabase();
        } catch (Exception e) {
            logger.error("解析 GPT 回傳 JSON 時發生未預期錯誤，內容: '{}'", extractedJsonString, e);
            logger.error("傳入 JsonUtil 的內容 (HEX): {}", toHex(extractedJsonString));
            return getFallbackFromDatabase();
        }
        logger.info("解析後餐廳數: {}", aiList.size());

        List<AiRestaurantModel> successList = batchScreenshotUtil.captureScreenshots(aiList);
        logger.info("總任務數: {}, 成功截圖: {} 個，因 URL 為空跳過: {} 個，截圖失敗: {} 個。",
                aiList.size(),
                (int) successList.stream().filter(r -> r.getPhotoUrl() != null && !r.getPhotoUrl().contains("No+URL") && !r.getPhotoUrl().contains("Error+Image")).count(),
                (int) successList.stream().filter(r -> r.getPhotoUrl() != null && r.getPhotoUrl().contains("No+URL")).count(),
                (int) successList.stream().filter(r -> r.getPhotoUrl() != null && r.getPhotoUrl().contains("Error+Image")).count());

        long actualSuccessCount = successList.stream()
                .filter(r -> r.getPhotoUrl() != null && !r.getPhotoUrl().contains("No+URL") && !r.getPhotoUrl().contains("Error+Image"))
                .count();

        if (actualSuccessCount == 0 && !aiList.isEmpty()) {
            logger.warn("AI 推薦全部截圖失敗或跳過，改用資料庫 fallback。");
            return getFallbackFromDatabase();
        }
        return successList;
    }

    private List<AiRestaurantModel> getFallbackFromDatabase() {
        logger.info("執行資料庫 fallback (從 RecommendService 獲取隨機推薦)。");
        List<RecommendDTO> recommends = recommendService.findRandom6Recommends();
        List<AiRestaurantModel> fallback = new ArrayList<>();

        for (RecommendDTO r : recommends) {
            fallback.add(new AiRestaurantModel(r.getStoreName(), r.getStoreAddress(), null, r.getStorePhotoUrl()));
        }
        return fallback;
    }

    // 將字符串轉換為十六進制表示，以便排查不可見字符
    private String toHex(String s) {
        if (s == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            sb.append(String.format("\\u%04x", (int) c));
        }
        return sb.toString();
    }
}