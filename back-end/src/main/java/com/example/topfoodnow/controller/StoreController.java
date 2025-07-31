package com.example.topfoodnow.controller;

import com.example.topfoodnow.service.StoreService;
import com.example.topfoodnow.dto.StoreWithAvgScoreDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

@RestController
@RequestMapping("/api/store")
@RequiredArgsConstructor
@Tag(name = "店家管理", description = "無須登入")
public class StoreController {
    private static final Logger logger = LoggerFactory.getLogger(StoreController.class);

    private final StoreService storeService;

    @Operation(summary = "隨機獲取指定數量的店家及其平均評分", description = "返回店家ID、名稱和基於其推薦的平均評分。")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功獲取店家列表"),
        @ApiResponse(responseCode = "400", description = "請求參數無效")
    })
    @GetMapping("/random")
    public ResponseEntity<List<StoreWithAvgScoreDTO>> getRandomStoresWithAverageScore(
            @Parameter(description = "要獲取的店家數量", required = false, example = "3")
            @RequestParam(defaultValue = "3") int limit) {
        if (limit <= 0) {
            logger.warn("請求的隨機店家數量無效: {}", limit);
            return ResponseEntity.badRequest().build();
        }
        logger.info("請求隨機獲取 {} 家店家及其平均評分", limit);
        List<StoreWithAvgScoreDTO> stores = storeService.getRandomStoresWithAverageScore(limit);
        return ResponseEntity.ok(stores);
    }
}