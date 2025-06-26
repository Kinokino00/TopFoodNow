package com.example.topfoodnow.util;

import com.example.topfoodnow.model.AiRestaurantModel;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class JsonUtil {
    public List<AiRestaurantModel> parseAiRestaurantList(String jsonText) {
        List<AiRestaurantModel> result = new ArrayList<>();
        try {
            JSONObject root = new JSONObject(jsonText);
            JSONArray choices = root.getJSONArray("choices");
            JSONObject message = choices.getJSONObject(0).getJSONObject("message");
            String content = message.getString("content");

            String arrayText = extractJsonArray(content);
            JSONArray arr = new JSONArray(arrayText);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                AiRestaurantModel info = new AiRestaurantModel();

                // 必須存在的欄位，使用 getString
                info.setName(obj.getString("name"));
                info.setUrl(obj.getString("url")); // AI目前有提供url

                // **關鍵修正點：使用 optString 安全地獲取可選欄位**
                // 如果 JSON 中沒有 "address"，則 info.address 會被設置為 null
                info.setAddress(obj.optString("address", null));

                // 如果 JSON 中沒有 "photoUrl"，則 info.photoUrl 會被設置為 null
                info.setPhotoUrl(obj.optString("photoUrl", null));

                result.add(info);
            }
        } catch (Exception e) {
            System.err.println("解析 AI 回傳 JSON 失敗！");
            System.err.println("原始 JSON 內容: " + jsonText); // 打印原始輸入方便偵錯
            e.printStackTrace();
        }
        return result;
    }

    private String extractJsonArray(String raw) {
        // 這個方法看起來是為了從 Markdown 格式的內容中提取 JSON 陣列
        // 確保它能正確處理 ````json ... ```` 這樣的格式
        int start = raw.indexOf("```json\n["); // 找尋 ````json` 開頭，後面是 `[`
        if (start == -1) { // 如果沒有 ````json` 標記，直接找 `[`
            start = raw.indexOf("[");
        } else {
            start += "```json\n".length(); // 跳過 ````json\n`
        }

        int end = raw.lastIndexOf("]");

        if (start >= 0 && end > start) {
            return raw.substring(start, end + 1);
        }
        System.err.println("未能從 GPT 回應中提取 JSON 陣列。原始內容: " + raw);
        return "[]"; // 返回空陣列，避免後續解析錯誤
    }
}
