package com.example.topfoodnow.controller;

import com.example.topfoodnow.service.StoreCategoryService;
import com.example.topfoodnow.dto.StoreCategoryDTO; // 用於單一關聯的 DTO
import com.example.topfoodnow.dto.StoreCategoryRequestDTO; // 新增：用於 POST 請求的 DTO
import com.example.topfoodnow.dto.StoreCategoryResponseDTO; // 新增：用於 GET 響應的 DTO
import com.example.topfoodnow.model.UserModel; // 新增：用於獲取用戶信息
import com.example.topfoodnow.service.UserService; // 新增：用於獲取用戶服務

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse; // 新增：用於 API 響應註解
import io.swagger.v3.oas.annotations.responses.ApiResponses; // 新增：用於 API 響應註解
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement; // 新增：用於 JWT 認證標識

import jakarta.validation.Valid; // 新增：用於 DTO 驗證
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize; // 新增：用於方法級別權限控制
import org.slf4j.Logger; // 新增：用於日誌
import org.slf4j.LoggerFactory; // 新增：用於日誌

import java.security.Principal; // 新增：用於獲取當前用戶
import java.util.List;
// import java.util.Map; // 移除：不再使用 Map 作為請求體

@RestController
@RequestMapping("/api/store-categories")
@Tag(name = "店家與分類管理", description = "店家與分類關係的 API")
public class StoreCategoryController {
    private static final Logger logger = LoggerFactory.getLogger(StoreCategoryController.class); // 新增日誌

    private final StoreCategoryService storeCategoryService;
    private final UserService userService; // 新增：注入 UserService

    public StoreCategoryController(StoreCategoryService storeCategoryService, UserService userService) {
        this.storeCategoryService = storeCategoryService;
        this.userService = userService;
    }

    /**
     * 獲取當前用戶 ID
     * @param principal Spring Security 提供的當前認證用戶資訊
     * @return 成功則獲取當前用戶 ID (Integer)；否則返回 null，表示用戶未登入或系統中不存在與 Principal 關聯的用戶
     */
    private Integer getCurrentUserId(Principal principal) {
        if (principal == null) {
            logger.debug("getCurrentUserId: Principal is null, user not logged in.");
            return null;
        }
        String principalName = principal.getName();
        return userService.findByEmail(principalName)
                .map(UserModel::getId)
                .orElseGet(() -> {
                    logger.warn("getCurrentUserId: UserService 找不到對應於郵箱 {} 的用戶。這可能表示數據不一致。", principalName);
                    return null;
                });
    }

    /**
     * 檢查當前用戶是否為管理員
     * @param principal 當前認證用戶資訊
     * @return 如果是管理員則返回 true，否則返回 false
     */
    private boolean isAdmin(Principal principal) {
        if (principal == null) {
            return false;
        }
        String principalName = principal.getName();
        return userService.findByEmail(principalName)
                .map(user -> user.getRole() != null && "ADMIN".equalsIgnoreCase(user.getRole().getName()))
                .orElse(false);
    }

    @Operation(summary = "取得指定店家的所有分類", description = "返回指定店家經過排序的分類列表。排序規則：管理員添加的分類優先，其次是根據用戶選擇次數排序。")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功獲取店家分類列表",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StoreCategoryResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "未找到指定店家")
    })
    @GetMapping("/by-store/{storeId}")
    public ResponseEntity<StoreCategoryResponseDTO> getCategoriesForStore(@PathVariable Integer storeId) {
        logger.info("請求獲取店家 ID: {} 的分類列表。", storeId);
        try {
            StoreCategoryResponseDTO response = storeCategoryService.getSortedCategoriesForStore(storeId);
            return ResponseEntity.ok(response);
        } catch (jakarta.persistence.EntityNotFoundException e) {
            logger.warn("未找到店家 ID: {}。", storeId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.error("獲取店家 ID: {} 分類時發生錯誤: {}", storeId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "取得指定分類下的所有店家", description = "返回指定分類下的所有店家列表。")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功獲取店家列表",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StoreCategoryDTO.class))),
            @ApiResponse(responseCode = "404", description = "未找到指定分類")
    })
    @GetMapping("/by-category/{categoryId}")
    public ResponseEntity<List<StoreCategoryDTO>> getStoresForCategory(@PathVariable Integer categoryId) {
        logger.info("請求獲取分類 ID: {} 下的所有店家。", categoryId);
        try {
            List<StoreCategoryDTO> stores = storeCategoryService.getStoresByCategoryId(categoryId);
            return ResponseEntity.ok(stores);
        } catch (jakarta.persistence.EntityNotFoundException e) {
            logger.warn("未找到分類 ID: {}。", categoryId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.error("獲取分類 ID: {} 下的店家時發生錯誤: {}", categoryId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "為店家添加或更新分類", description = "需要 ADMIN 權限。為指定店家添加多個分類。如果分類已存在，則更新其關聯。管理員添加的分類將在排序時優先。")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "分類添加/更新成功",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StoreCategoryResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "請求數據無效或分類/店家不存在"),
            @ApiResponse(responseCode = "401", description = "未經認證"),
            @ApiResponse(responseCode = "403", description = "無權限 (非管理員)")
    })
    @SecurityRequirement(name = "bearerAuth") // 指示此 API 需要 JWT 認證
    @PreAuthorize("hasRole('ADMIN')") // 只有 ADMIN 角色才能調用此方法
    @PostMapping
    public ResponseEntity<StoreCategoryResponseDTO> addOrUpdateCategoriesToStore(
            @Valid @RequestBody StoreCategoryRequestDTO request,
            Principal principal) { // 接收 Principal 以判斷是否為 ADMIN
        logger.info("管理員嘗試為店家 ID: {} 添加或更新分類。", request.getStoreId());
        try {
            // isAdmin 方法已經判斷了角色，但 @PreAuthorize 已經確保了 ADMIN 權限
            // 這裡可以再次確認，或者信任 @PreAuthorize
            boolean isAdminRequest = isAdmin(principal); // 判斷是否為管理員發起的請求

            StoreCategoryResponseDTO updatedStoreCategories = storeCategoryService.addOrUpdateStoreCategories(
                    request.getStoreId(),
                    request.getCategoryIds(),
                    isAdminRequest
            );
            return ResponseEntity.ok(updatedStoreCategories); // 返回更新後的店家分類列表
        } catch (jakarta.persistence.EntityNotFoundException e) {
            logger.warn("添加分類失敗：店家或分類不存在。錯誤: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (IllegalArgumentException e) {
            logger.warn("添加分類失敗：請求數據無效。錯誤: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            logger.error("為店家 ID: {} 添加分類時發生錯誤: {}", request.getStoreId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "從店家移除分類", description = "需要 ADMIN 權限。從指定店家移除一個分類關聯。")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "分類成功移除"),
            @ApiResponse(responseCode = "400", description = "請求數據無效"),
            @ApiResponse(responseCode = "401", description = "未經認證"),
            @ApiResponse(responseCode = "403", description = "無權限 (非管理員)"),
            @ApiResponse(responseCode = "404", description = "未找到該關聯")
    })
    @SecurityRequirement(name = "bearerAuth") // 指示此 API 需要 JWT 認證
    @PreAuthorize("hasRole('ADMIN')") // 只有 ADMIN 角色才能調用此方法
    @DeleteMapping
    public ResponseEntity<Void> removeCategoryFromStore(
            @RequestParam @Schema(description = "店家ID", example = "1") Integer storeId,
            @RequestParam @Schema(description = "分類ID", example = "5") Integer categoryId) {
        logger.info("管理員嘗試從店家 ID: {} 移除分類 ID: {}。", storeId, categoryId);
        try {
            storeCategoryService.removeStoreCategory(storeId, categoryId);
            return ResponseEntity.noContent().build();
        } catch (jakarta.persistence.EntityNotFoundException e) {
            logger.warn("移除分類失敗：未找到店家 ID: {} 與分類 ID: {} 的關聯。錯誤: {}", storeId, categoryId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("從店家 ID: {} 移除分類 ID: {} 時發生錯誤: {}", storeId, categoryId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}