package com.example.topfoodnow.util;

import com.example.topfoodnow.model.AiRestaurantModel;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class JsonUtil {

    private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);

    public List<AiRestaurantModel> parseRestaurants(String jsonString) {
        List<AiRestaurantModel> restaurants = new ArrayList<>();
        try {
            String cleanedJson = jsonString;
            JSONArray jsonArray = new JSONArray(cleanedJson);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                AiRestaurantModel restaurant = new AiRestaurantModel();

                restaurant.setName(jsonObject.optString("name", "未知名稱"));
                restaurant.setAddress(jsonObject.optString("address", "未知地址"));
                restaurant.setUrl(jsonObject.optString("url"));

                if (restaurant.getUrl() == null || restaurant.getUrl().trim().isEmpty()) {
                    logger.warn("JsonUtil 發現餐廳 '{}' 的 URL 為空或無效。", restaurant.getName());
                } else {
                    logger.debug("JsonUtil 成功解析餐廳 '{}' 的 URL: {}", restaurant.getName(), restaurant.getUrl());
                }
                restaurants.add(restaurant);
            }
        } catch (Exception e) {
            logger.error("解析餐廳 JSON 失敗，原始 JSON: '{}'，錯誤: {}", jsonString, e.getMessage(), e);
        }
        return restaurants;
    }
}