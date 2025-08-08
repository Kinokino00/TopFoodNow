package com.example.topfoodnow.controller;

import com.example.topfoodnow.service.StoreService;
import com.example.topfoodnow.dto.StoreDetailDTO;
import com.example.topfoodnow.dto.StoreWithAvgScoreDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

@RestController
@RequestMapping("/api/store")
@RequiredArgsConstructor
@Tag(name = "店家管理")
public class StoreController {
    private static final Logger logger = LoggerFactory.getLogger(StoreController.class);

    private final StoreService storeService;

    @Operation(summary = "隨機取得指定數量的店家及其平均評分 (無須認證)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功取得店家列表"),
        @ApiResponse(responseCode = "400", description = "請求參數無效")
    })
    @GetMapping("/random")
    public ResponseEntity<List<StoreWithAvgScoreDTO>> getRandomStoresWithAverageScore(
            @Parameter(description = "要取得的店家數量", required = false, example = "3")
            @RequestParam(defaultValue = "3") int limit) {
        if (limit <= 0) {
            logger.warn("請求的隨機店家數量無效: {}", limit);
            return ResponseEntity.badRequest().build();
        }
        logger.info("請求隨機取得 {} 家店家及其平均評分", limit);
        List<StoreWithAvgScoreDTO> stores = storeService.getRandomStoresWithAverageScore(limit);
        return ResponseEntity.ok(stores);
    }

    @Operation(summary = "取得指定店家的詳細資訊", description = "返回指定店家的名稱、地址、平均評分和分類名稱列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功取得店家詳細資訊"),
        @ApiResponse(responseCode = "404", description = "未找到指定店家")
    })
    @GetMapping("/{storeId}/details")
    public ResponseEntity<StoreDetailDTO> getStoreDetails(@PathVariable Integer storeId) {
        logger.info("請求取得店家 ID: {} 的詳細資訊。", storeId);
        try {
            StoreDetailDTO response = storeService.getStoreDetails(storeId);
            return ResponseEntity.ok(response);
        } catch (jakarta.persistence.EntityNotFoundException e) {
            logger.warn("未找到店家 ID: {}。", storeId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.error("取得店家 ID: {} 詳細資訊時發生錯誤: {}", storeId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}