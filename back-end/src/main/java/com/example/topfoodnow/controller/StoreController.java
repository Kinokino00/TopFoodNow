package com.example.topfoodnow.controller;

import com.example.topfoodnow.model.StoreModel;
import com.example.topfoodnow.payload.request.StoreCreateRequest;
import com.example.topfoodnow.payload.request.StoreUpdateRequest;
import com.example.topfoodnow.payload.response.StoreResponse;
import com.example.topfoodnow.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/store")
@RequiredArgsConstructor
@Tag(name = "店家管理", description = "店家相關操作 API")
public class StoreController {
    private final StoreService storeService;

    @Operation(summary = "獲取隨機 3 個店家",
        responses = {
            @ApiResponse(responseCode = "200", description = "成功獲取店家列表")
        })
    @GetMapping("/random")
    public ResponseEntity<List<StoreModel>> get3Stores() {
        return ResponseEntity.ok(storeService.findRandom3Stores());
    }

    @Operation(summary = "通過ID獲取店家",
        responses = {
            @ApiResponse(responseCode = "200", description = "成功獲取店家"),
            @ApiResponse(responseCode = "404", description = "未找到店家")
        })
    @GetMapping("/{id}")
    public ResponseEntity<StoreResponse> getStoreById(@PathVariable Integer id) {
        Optional<StoreModel> storeOptional = storeService.getStoreById(id);
        return storeOptional.map(store -> ResponseEntity.ok(new StoreResponse(store)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "創建新店家",
        responses = {
            @ApiResponse(responseCode = "201", description = "店家創建成功"),
            @ApiResponse(responseCode = "400", description = "請求數據無效或類別不存在")
        })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StoreModel> createStore(
            @ModelAttribute @Valid StoreCreateRequest request) {
        try {
            StoreModel createdStore = storeService.createStore(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdStore);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @Operation(summary = "更新店家資料",
        responses = {
            @ApiResponse(responseCode = "200", description = "店家更新成功"),
            @ApiResponse(responseCode = "400", description = "請求數據無效或類別不存在"),
            @ApiResponse(responseCode = "404", description = "未找到店家")
        })
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StoreModel> updateStore(
            @PathVariable Integer id,
            @ModelAttribute @Valid StoreUpdateRequest request) {
        try {
            StoreModel updatedStore = storeService.updateStore(id, request);
            return ResponseEntity.ok(updatedStore);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @Operation(summary = "刪除店家",
        responses = {
            @ApiResponse(responseCode = "204", description = "店家刪除成功"),
            @ApiResponse(responseCode = "404", description = "未找到店家")
        })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStore(@PathVariable Integer id) {
        try {
            storeService.deleteStore(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}