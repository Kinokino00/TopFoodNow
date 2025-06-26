package com.example.topfoodnow.controller;

import com.example.topfoodnow.model.AiRestaurantModel;
import com.example.topfoodnow.util.BatchScreenshotUtil;
import com.example.topfoodnow.util.ChatGPTUtil;
import com.example.topfoodnow.util.JsonUtil;
import com.example.topfoodnow.repository.StoreRepository;
import com.example.topfoodnow.model.StoreModel;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class AiRestaurantController {
    private final JsonUtil jsonUtil;
    private final BatchScreenshotUtil batchScreenshotUtil;
    private final StoreRepository storeRepository;

    @Value("${screenshot.output.dir}")
    private String screenshotDir;

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
        "[{\"name\": \"鼎泰豐\", \"address\": \"台北市信義路二段22號\", \"url\": \"[https://www.dintaifung.com.tw/](https://www.dintaifung.com.tw/)\"}]";

    @GetMapping
    public List<AiRestaurantModel> getAIRecommend() throws IOException {
        System.out.println("/api/ai 啟動 AI 搜尋");

        String gptResponseJson = ChatGPTUtil.askGpt(PROMPT, apiKey);
        System.out.println("GPT 回傳 JSON: " + gptResponseJson);

        List<AiRestaurantModel> aiList;
        try {
            aiList = jsonUtil.parseAiRestaurantList(gptResponseJson);
        } catch (Exception e) {
            System.err.println("解析 GPT 回傳 JSON 失敗！原始 JSON 內容: " + gptResponseJson);
            e.printStackTrace(); // 打印完整的堆疊追蹤，查看解析失敗的原因
            return getFallbackFromDatabase(); // 解析失敗，直接回退
        }

        System.out.println("解析後餐廳數: " + aiList.size());

        List<AiRestaurantModel> successList = batchScreenshotUtil.captureScreenshots(aiList, screenshotDir);
        System.out.println("成功截圖數: " + successList.size());

        if (successList.isEmpty()) {
            System.out.println("全部失敗，改用資料庫 fallback");
            return getFallbackFromDatabase();
        }

        return successList;
    }

    private List<AiRestaurantModel> getFallbackFromDatabase() {
        List<StoreModel> stores = storeRepository.findRandom6Stores();
        List<AiRestaurantModel> fallback = new ArrayList<>();

        for (StoreModel s : stores) {
            fallback.add(new AiRestaurantModel(
                s.getName(),
                s.getAddress(),
                s.getPhotoUrl(),
                null
            ));
        }

        return fallback;
    }
}
