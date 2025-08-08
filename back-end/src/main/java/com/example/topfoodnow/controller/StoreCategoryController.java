package com.example.topfoodnow.controller;

import com.example.topfoodnow.dto.StoreCategoryRequestDTO;
import com.example.topfoodnow.dto.StoreCategoryResponseDTO;
import com.example.topfoodnow.service.StoreCategoryService;
import com.example.topfoodnow.model.UserModel;
import com.example.topfoodnow.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.security.Principal;

@RestController
@RequestMapping("/api/store-categories")
@Tag(name = "店家與分類管理")
public class StoreCategoryController {
    private static final Logger logger = LoggerFactory.getLogger(StoreCategoryController.class);

    private final StoreCategoryService storeCategoryService;
    private final UserService userService;

    public StoreCategoryController(StoreCategoryService storeCategoryService, UserService userService) {
        this.storeCategoryService = storeCategoryService;
        this.userService = userService;
    }

    /**
     * 取得當前用戶 ID
     * @param principal Spring Security 提供的當前認證用戶資訊
     * @return 成功則取得當前用戶 ID (Integer)；否則返回 null，表示用戶未登入或系統中不存在與 Principal 關聯的用戶
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

    @Operation(summary = "為店家添加或更新分類", description = "需要 ADMIN 權限。為指定店家添加多個分類。如果分類已存在，則更新其關聯。管理員添加的分類將在排序時優先")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "分類添加/更新成功",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = StoreCategoryResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "請求數據無效或分類/店家不存在"),
        @ApiResponse(responseCode = "401", description = "未經認證"),
        @ApiResponse(responseCode = "403", description = "無權限 (非管理員)")
    })
    @SecurityRequirement(name = "bearerAuth") // 此 API 需要 JWT 認證
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<StoreCategoryResponseDTO> addOrUpdateCategoriesToStore(
            @Valid @RequestBody StoreCategoryRequestDTO request,
            Principal principal) {
        logger.info("管理員嘗試為店家 ID: {} 添加或更新分類。", request.getStoreId());
        try {
            boolean isAdminRequest = isAdmin(principal);
            StoreCategoryResponseDTO updatedStoreCategories = storeCategoryService.addOrUpdateStoreCategories(
                    request.getStoreId(),
                    request.getCategoryIds(),
                    isAdminRequest
            );
            return ResponseEntity.ok(updatedStoreCategories);
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

    @Operation(summary = "從店家移除分類", description = "需要 ADMIN 權限")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "分類成功移除"),
        @ApiResponse(responseCode = "400", description = "請求數據無效"),
        @ApiResponse(responseCode = "401", description = "未經認證"),
        @ApiResponse(responseCode = "403", description = "無權限 (非管理員)"),
        @ApiResponse(responseCode = "404", description = "未找到該關聯")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
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