package com.example.topfoodnow.controller.ai;

import com.example.topfoodnow.model.AiRestaurantModel;
import com.example.topfoodnow.common.util.BatchScreenshotUtil;
import com.example.topfoodnow.common.util.AiUtil;
import com.example.topfoodnow.common.util.JsonUtil;
import com.example.topfoodnow.controller.recommend.dto.Recommend;
import com.example.topfoodnow.service.recommend.RecommendService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONObject;
import org.json.JSONArray;
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

    @Value("${ai.api.key}")
    private String apiKey;

    public AiRestaurantController(JsonUtil jsonUtil,
                                  BatchScreenshotUtil batchScreenshotUtil,
                                  RecommendService recommendService) {
        this.jsonUtil = jsonUtil;
        this.batchScreenshotUtil = batchScreenshotUtil;
        this.recommendService = recommendService;
    }

    private static final String PROMPT =
        "è«‹è¼¸?ºå°??å®¶ç†±?€ç¾é?é¤å»³ï¼Œå??«å?ç¨±ã€åœ°?€?Œç¶²?€(?¡é??ä?ç©©å??¯è¨ª?ç??ç¤¾ç¾¤å?é«”å??¹ç¶²ç«??‚ä½¿??JSON ????¼å??å‚³ï¼Œä?å¦‚ï?\n" +
        "[{\"name\": \"é¼æ³°è±\", \"address\": \"?°å?å¸‚ä¿¡ç¾©è·¯äºŒæ®µ22?Ÿ\", \"url\": \"https://www.dintaifung.com.tw/\"}]\n" +
        "è«‹ç¢ºä¿å??³å…§å®¹ç‚ºç´?JSON ???ï¼Œä??…å«ä»»ä?é¡å??‡å???markdown ?¼å?ç¬¦è???; // ?å?Gemini?ªå??ç¤ºï¼Œè?æ±‚ç?JSON

    @Operation(
        summary = "?²å? AI ?¨è–¦é¤å»³?—è¡¨",
        description = "?¼å« Google Gemini API ?Ÿæ??°å?6å®¶ç†±?€ç¾é?é¤å»³?¨è–¦ï¼Œä¸¦?—è©¦?²è??ªå??‚å???AI ?å‚³è§??å¤±æ??–æˆª?–å¤±?—ï??‡å?è³‡æ?åº«ä¸­?ä??™ç”¨é¤å»³?—è¡¨??,
        responses = {
            @ApiResponse(
                    responseCode = "200",
                    description = "?å??²å? AI ?¨è–¦?–è??™åº«?™ç”¨é¤å»³?—è¡¨",
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = AiRestaurantModel.class, type = "array")
                    )
            ),
            @ApiResponse(
                responseCode = "500",
                description = "ä¼ºæ??¨å…§?¨éŒ¯èª¤ï?ä¾‹å???Google Gemini API ?šè?å¤±æ??–æˆª?–æ??™ç•°å¸?,
                content = @Content(schema = @Schema(implementation = String.class))
            )
        }
    )
    @GetMapping
    public List<AiRestaurantModel> getAIRecommend() throws IOException {
        logger.info("?Ÿå? AI ?œå? (Gemini)");

        String geminiFullResponseJson = AiUtil.askAi(PROMPT, apiKey);
        logger.debug("Gemini ?å‚³?Ÿå? JSON: {}", geminiFullResponseJson);

        String geminiContent = null;
        try {
            JSONObject fullGeminiResponse = new JSONObject(geminiFullResponseJson);
            geminiContent = fullGeminiResponse
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text");

            logger.debug("Gemini content ?Ÿå??§å®¹ (å·²è???: {}", geminiContent);
            logger.debug("Gemini content ?Ÿå??§å®¹ (HEX): {}", toHex(geminiContent));
        } catch (JSONException e) {
            logger.error("?¡æ?è§?? Gemini ?Ÿå??å‚³ JSON ?–æ???'content' æ¬„ä???, e);
            logger.error("Gemini ?Ÿå??å‚³ JSON (HEX): {}", toHex(geminiFullResponseJson));
            return getFallbackFromDatabase();
        }

        String extractedJsonString = null;
        Pattern pattern = Pattern.compile("```json\\s*\\n([\\s\\S]*?)\\n```");
        Matcher matcher = pattern.matcher(geminiContent);

        if (matcher.find()) {
            extractedJsonString = matcher.group(1);
            logger.debug("å¾?Markdown ?€å¡Šä¸­?å???JSON å­—ä¸² (?æ­¥): {}", extractedJsonString);
            logger.debug("å¾?Markdown ?€å¡Šä¸­?å???JSON å­—ä¸² (?æ­¥ HEX): {}", toHex(extractedJsonString));

            extractedJsonString = extractedJsonString.replaceAll("[\\p{C}&&[^\t\n\r]]", "");
            extractedJsonString = extractedJsonString.trim();
        } else {
            extractedJsonString = geminiContent.trim();
            extractedJsonString = extractedJsonString.replaceAll("[\\p{C}&&[^\t\n\r]]", "");
            logger.debug("?¡æ?å¾?Markdown ?€å¡Šä¸­?å?ï¼Œç›´?¥ä½¿?¨æ??†å???Gemini content ä½œç‚º JSON å­—ä¸²: {}", extractedJsonString);
            logger.debug("?´æ¥ä½¿ç”¨??JSON å­—ä¸² (HEX): {}", toHex(extractedJsonString));

            if (extractedJsonString.startsWith("```json") || extractedJsonString.endsWith("```")) {
                logger.error("?³ä½¿?´æ¥ä½¿ç”¨ï¼Œæ??†å???Gemini content ä»ç„¶?…å« Markdown æ¨™è??‚é€™è¡¨ç¤ºè§£?é?è¼¯å¯?½é?è¦é€²ä?æ­¥èª¿?´ã€‚å…§å®? '{}'", extractedJsonString);
                return getFallbackFromDatabase();
            }
        }

        List<AiRestaurantModel> aiList;
        try {
            logger.debug("Gemini ?å‚³?€çµ‚æ??†å? JSON (?³çµ¦ JsonUtil ?„å…§å®?: {}", extractedJsonString);
            logger.debug("Gemini ?å‚³?€çµ‚æ??†å? JSON (HEX): {}", toHex(extractedJsonString));

            aiList = jsonUtil.parseRestaurants(extractedJsonString);
        } catch (JSONException e) {
            logger.error("è§?? Gemini ?å‚³ JSON å¤±æ?ï¼Œå‚³??JsonUtil ?„å…§å®¹æ?èª¤ã€‚å…§å®? '{}'", extractedJsonString, e);
            logger.error("?³å…¥ JsonUtil ?„å…§å®?(HEX): {}", toHex(extractedJsonString));
            return getFallbackFromDatabase();
        } catch (Exception e) {
            logger.error("è§?? Gemini ?å‚³ JSON ?‚ç™¼?Ÿæœª?æ??¯èª¤ï¼Œå…§å®? '{}'", extractedJsonString, e);
            logger.error("?³å…¥ JsonUtil ?„å…§å®?(HEX): {}", toHex(extractedJsonString));
            return getFallbackFromDatabase();
        }
        logger.info("è§??å¾Œé?å»³æ•¸: {}", aiList.size());

        List<AiRestaurantModel> successList = batchScreenshotUtil.captureScreenshots(aiList);
        logger.info("ç¸½ä»»?™æ•¸: {}, ?å??ªå?: {} ?‹ï???URL ?ºç©ºè·³é?: {} ?‹ï??ªå?å¤±æ?: {} ?‹ã€?,
                aiList.size(),
                (int) successList.stream().filter(r -> r.getPhotoUrl() != null && !r.getPhotoUrl().contains("No+URL") && !r.getPhotoUrl().contains("Error+Image")).count(),
                (int) successList.stream().filter(r -> r.getPhotoUrl() != null && r.getPhotoUrl().contains("No+URL")).count(),
                (int) successList.stream().filter(r -> r.getPhotoUrl() != null && r.getPhotoUrl().contains("Error+Image")).count());

        long actualSuccessCount = successList.stream()
                .filter(r -> r.getPhotoUrl() != null && !r.getPhotoUrl().contains("No+URL") && !r.getPhotoUrl().contains("Error+Image"))
                .count();

        if (actualSuccessCount == 0 && !aiList.isEmpty()) {
            logger.warn("AI ?¨è–¦?¨éƒ¨?ªå?å¤±æ??–è·³?ï??¹ç”¨è³‡æ?åº?fallback??);
            return getFallbackFromDatabase();
        }
        return successList;
    }

    private List<AiRestaurantModel> getFallbackFromDatabase() {
        logger.info("?·è?è³‡æ?åº?fallback (å¾?RecommendService ?²å??¨æ??¨è–¦)??);
        List<Recommend> recommends = recommendService.findRandom6Recommends();
        List<AiRestaurantModel> fallback = new ArrayList<>();

        for (Recommend r : recommends) {
            fallback.add(new AiRestaurantModel(r.getStoreName(), r.getStoreAddress(), null, r.getStorePhotoUrl()));
        }
        return fallback;
    }

    // å°‡å?ç¬¦ä¸²è½‰æ??ºå??­é€²åˆ¶è¡¨ç¤ºï¼Œä»¥ä¾¿æ??¥ä??¯è?å­—ç¬¦
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
