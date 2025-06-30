package com.example.topfoodnow.controller;

import com.example.topfoodnow.model.AiRestaurantModel;
import com.example.topfoodnow.util.BatchScreenshotUtil;
import com.example.topfoodnow.util.ChatGPTUtil;
import com.example.topfoodnow.util.JsonUtil;
import com.example.topfoodnow.repository.StoreRepository;
import com.example.topfoodnow.model.StoreModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class AiRestaurantController {
    private static final Logger logger = LoggerFactory.getLogger(AiRestaurantController.class); // 新增 Logger
    private final JsonUtil jsonUtil;
    private final BatchScreenshotUtil batchScreenshotUtil;
    private final StoreRepository storeRepository;

    @Value("${openai.api.key}")
    private String apiKey;

    public AiRestaurantController(JsonUtil jsonUtil,
          BatchScreenshotUtil batchScreenshotUtil,
          StoreRepository storeRepository) {
        this.jsonUtil = jsonUtil;
        this.batchScreenshotUtil = batchScreenshotUtil;
        this.storeRepository = storeRepository;
    }

    private static final String PROMPT =
        "請輸出台北16家熱門美食餐廳，包含名稱、地址和網址(盡量提供穩定可訪問的非社群媒體官方網站)。使用 JSON 陣列格式回傳，例如：\n" +
        "[{\"name\": \"鼎泰豐\", \"address\": \"台北市信義路二段22號\", \"url\": \"https://www.dintaifung.com.tw/\"}]";

    @GetMapping
    public List<AiRestaurantModel> getAIRecommend() throws IOException {
        logger.info("/api/ai 啟動 AI 搜尋");

        String gptResponseJson = ChatGPTUtil.askGpt(PROMPT, apiKey);
        logger.debug("GPT 回傳 JSON: {}", gptResponseJson);

        List<AiRestaurantModel> aiList;
        try {
            aiList = jsonUtil.parseAiRestaurantList(gptResponseJson);
        } catch (Exception e) {
            logger.error("解析 GPT 回傳 JSON 失敗！原始 JSON 內容: {}", gptResponseJson, e);
            return getFallbackFromDatabase();
        }

        logger.info("解析後餐廳數: {}", aiList.size());

        List<AiRestaurantModel> successList = batchScreenshotUtil.captureScreenshots(aiList);
        logger.info("成功截圖數: {}", successList.size());

        if (successList.isEmpty()) {
            logger.warn("AI 推薦全部截圖失敗，改用資料庫 fallback");
            return getFallbackFromDatabase();
        }
        return successList;
    }

    private List<AiRestaurantModel> getFallbackFromDatabase() {
        List<StoreModel> stores = storeRepository.findRandom6Stores();
        List<AiRestaurantModel> fallback = new ArrayList<>();

        for (StoreModel s : stores) {
            fallback.add(new AiRestaurantModel(s.getName(), s.getAddress(), s.getPhotoUrl(), null));
        }
        return fallback;
    }
}