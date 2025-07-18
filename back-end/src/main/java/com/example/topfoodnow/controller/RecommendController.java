package com.example.topfoodnow.controller;

import com.example.topfoodnow.model.UserModel;
import com.example.topfoodnow.dto.RecommendDTO;
import com.example.topfoodnow.service.RecommendService;
import com.example.topfoodnow.service.UserService;
import com.example.topfoodnow.service.FileStorageService;
import com.example.topfoodnow.dto.CustomPageResponseDTO;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/recommend")
@Tag(name = "推薦管理")
public class RecommendController {
    private static final Logger logger = LoggerFactory.getLogger(RecommendController.class);

    @Autowired
    private RecommendService recommendService;

    @Autowired
    private UserService userService;

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * 獲取當前用戶 ID
     * @Parm 當前用戶
     * @return 成功則獲取當前用戶 ID (Integer)；否則返回 null，表示用戶未登入或系統中不存在與 Principal 關聯的用戶
     * */
    private Integer getCurrentUserId(Principal principal) {
        if (principal == null) {
            logger.debug("getCurrentUserId: Principal is null, user not logged in.");
            return null;
        }
        String principalName = principal.getName();
        Optional<UserModel> userOptional = userService.findByEmail(principalName);
        if (userOptional.isPresent()) {
            logger.debug("getCurrentUserId: Found UserModel for email {}, ID: {}", principalName, userOptional.get().getId());
            return userOptional.get().getId();
        } else {
            logger.warn("getCurrentUserId: UserService 找不到對應於郵箱 {} 的用戶。這可能表示數據不一致。", principalName);
            return null;
        }
    }

    // region 首頁
    @Operation(summary = "隨機餐廳推薦", description = "無需認證。返回6個隨機的餐廳推薦，用於首頁展示。")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功獲取隨機推薦列表")
    })
    @GetMapping("/random")
    public ResponseEntity<List<RecommendDTO>> getRandomRecommends() {
        logger.info("請求獲取隨機6個餐廳推薦。");
        List<RecommendDTO> randomRecommends = recommendService.findRandom6Recommends();
        return ResponseEntity.ok(randomRecommends);
    }

    @Operation(summary = "最新網紅推薦", description = "無需認證。返回指定數量 (預設6個) 的最新網紅推薦，用於首頁展示。")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功獲取最新網紅推薦列表")
    })
    @GetMapping("/famous")
    public ResponseEntity<List<RecommendDTO>> getFamousUserLatestRecommends(
            @Parameter(description = "要獲取的推薦數量，預設為6", example = "6")
            @RequestParam(defaultValue = "6") int limit) {
        logger.info("請求獲取最新 {} 個網紅推薦。", limit);
        List<RecommendDTO> famousUserRecommends = recommendService.findLatestFamousUserRecommends(limit);
        return ResponseEntity.ok(famousUserRecommends);
    }
    // endregion

    // region 所有餐廳推薦
     @Operation(summary = "所有餐廳推薦")
     @ApiResponse(responseCode = "200", description = "成功獲取分頁推薦列表")
     @GetMapping("/all")
     public ResponseEntity<CustomPageResponseDTO<RecommendDTO>> getAllRecommends(
             @RequestParam(defaultValue = "1") int page,
             @RequestParam(defaultValue = "10") int size,
             @RequestParam(defaultValue = "id,asc") String[] sort
     ) {
         logger.info("請求獲取所有餐廳推薦，頁碼: {}, 大小: {}, 排序: {}", page, size, sort);

         // 步驟 1: 將前端的 1-based 頁碼轉換為 Spring Data JPA 的 0-based 頁碼
         int springPage = page - 1;
         if (springPage < 0) {
             springPage = 0;
         }

         // 解析排序參數
         Sort springSort = Sort.unsorted();
         if (sort.length > 0) {
             try {
                 String property = sort[0];
                 Sort.Direction direction = Sort.Direction.ASC;
                 if (sort.length > 1 && sort[1].equalsIgnoreCase("desc")) {
                     direction = Sort.Direction.DESC;
                 }
                 springSort = Sort.by(direction, property);
             } catch (Exception e) {
                 logger.warn("解析排序參數失敗，使用預設排序。錯誤: {}", e.getMessage());
             }
         }

         // 創建 Spring Data JPA 的 Pageable 物件
         Pageable pageable = PageRequest.of(springPage, size, springSort);

         // 呼叫 Service 層獲取分頁數據
         Page<RecommendDTO> recommendPage = recommendService.findAllRecommendsPaged(
             springPage, size, null, null
         );

         // 步驟 2: 將 Spring Data JPA 的 Page 轉換為您的 CustomPageResponse 格式
         CustomPageResponseDTO<RecommendDTO> response = new CustomPageResponseDTO<>();
         response.setContent(recommendPage.getContent());
         response.setTotalElements(recommendPage.getTotalElements());
         response.setTotalPages(recommendPage.getTotalPages());

         CustomPageResponseDTO.CustomPageableInfo pageableInfo = new CustomPageResponseDTO.CustomPageableInfo();
         pageableInfo.setPageNumber(recommendPage.getNumber() + 1); // 將 0-based 頁碼轉換回 1-based
         pageableInfo.setPageSize(recommendPage.getSize());
         pageableInfo.setSort(springSort.isSorted() ? springSort.toString() : "unsorted");

         response.setPageable(pageableInfo);

         return ResponseEntity.ok(response);
     }
    // endregion

    // region 指定用戶的所有個人推薦
    @Operation(summary = "指定用戶的所有個人推薦", description = "無需認證。返回指定用戶的所有餐廳推薦列表。")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功獲取指定用戶的推薦列表"),
        @ApiResponse(responseCode = "404", description = "未找到指定用戶")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RecommendDTO>> getRecommendsBySpecificUserId(
            @Parameter(description = "用戶 ID", required = true, example = "1", in = ParameterIn.PATH)
            @PathVariable Integer userId) {
        if (!userService.findById(userId).isPresent()) {
            logger.warn("嘗試獲取推薦：用戶 ID: {} 不存在。", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        List<RecommendDTO> recommends = recommendService.getRecommendsByUserId(userId);
        logger.info("成功獲取用戶 ID: {} 的所有推薦列表。", userId);
        return ResponseEntity.ok(recommends);
    }
    // endregion

    // region 指定用戶對特定店家的推薦詳情
    @Operation(summary = "指定用戶對特定店家的推薦詳情", description = "無需認證。返回指定用戶對指定店家的推薦詳細資訊。可用於公開分享。")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功獲取推薦詳情"),
        @ApiResponse(responseCode = "404", description = "未找到該推薦")
    })
    @GetMapping("/{userId}/{storeId}") // GET /api/recommends/{userId}/{storeId}
    public ResponseEntity<RecommendDTO> getSpecificUserRecommendDetail(
            @Parameter(description = "用戶 ID", required = true, example = "1", in = ParameterIn.PATH)
            @PathVariable Integer userId,
            @Parameter(description = "店家 ID", required = true, example = "1", in = ParameterIn.PATH)
            @PathVariable Integer storeId) {
        logger.info("嘗試查看指定用戶 ID: {} 對店家 ID: {} 的推薦詳情。", userId, storeId);

        Optional<RecommendDTO> recommendOptional = recommendService.getRecommendByUserAndStoreId(userId, storeId);
        if (recommendOptional.isPresent()) {
            logger.info("找到推薦，返回詳情。");
            return ResponseEntity.ok(recommendOptional.get());
        } else {
            logger.warn("未找到用戶 ID: {} 對店家 ID: {} 的推薦，返回 404。", userId, storeId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    // endregion

    // region 新增餐廳推薦
    @Operation(
        summary = "新增餐廳推薦",
        description = "需認證。接收當前認證用戶提交的餐廳推薦信息（包含店家詳情和推薦原因/評分，可選上傳圖片）。",
        requestBody = @RequestBody(
            description = "推薦信息和可選的店家圖片",
            required = true,
            content = @Content(
                mediaType = "multipart/form-data",
                schema = @Schema(implementation = RecommendDTO.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "201", description = "推薦新增成功"),
            @ApiResponse(responseCode = "400", description = "請求數據無效或缺少必要字段"),
            @ApiResponse(responseCode = "401", description = "未經認證"),
            @ApiResponse(responseCode = "500", description = "伺服器內部錯誤")
        }
    )
    @PostMapping("/add-recommend")
    public ResponseEntity<RecommendDTO> addRecommendProcess(
            @Valid @ModelAttribute("recommendDTO") RecommendDTO recommendDTO,
            BindingResult bindingResult,
            Principal principal) {
        Integer currentUserId = getCurrentUserId(principal);
        if (currentUserId == null) {
            logger.warn("未認證用戶嘗試新增推薦。");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 手動驗證
        if (recommendDTO.getStorePhoto() == null || recommendDTO.getStorePhoto().isEmpty()) {
            if (recommendDTO.getStorePhotoUrl() == null || recommendDTO.getStorePhotoUrl().isEmpty()) {
                bindingResult.rejectValue("storePhoto", "error.recommendDTO", "請上傳店家圖片！");
            }
        }
        if (recommendDTO.getReason() == null || recommendDTO.getReason().trim().isEmpty()) {
            bindingResult.rejectValue("reason", "error.recommendDTO", "推薦原因不能為空！");
        }
        if (recommendDTO.getScore() == null || recommendDTO.getScore() < 1 || recommendDTO.getScore() > 5) {
            bindingResult.rejectValue("score", "error.recommendDTO", "請選擇有效的星級評分！");
        }


        if (bindingResult.hasErrors()) {
            logger.warn("新增推薦表單驗證失敗。");
            return ResponseEntity.badRequest().body(recommendDTO);
        }

        try {
            if (recommendDTO.getStorePhoto() != null && !recommendDTO.getStorePhoto().isEmpty()) {
                String photoUrl = fileStorageService.uploadFile(recommendDTO.getStorePhoto());
                recommendDTO.setStorePhotoUrl(photoUrl);
            }
            UserModel currentUserModel = userService.findById(currentUserId)
                    .orElseThrow(() -> new EntityNotFoundException("當前用戶不存在或未找到。"));
            recommendService.addRecommend(recommendDTO, currentUserModel);
            logger.info("用戶 {} 新增推薦成功。", currentUserModel.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(recommendDTO);
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            logger.error("新增推薦失敗：{}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            logger.error("新增推薦失敗：{}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    // endregion

    // region 更新指定推薦
    @Operation(
        summary = "更新指定推薦",
        description = "需認證。更新當前登入用戶對指定店家的推薦。",
        parameters = {
            @Parameter(name = "storeId", description = "要更新的店家ID", required = true, example = "101", in = ParameterIn.PATH)
        },
        requestBody = @RequestBody(
            description = "更新後的推薦信息和可選的店家圖片。圖片通過 multipart/form-data 提交。",
            required = true,
            content = @Content(
                mediaType = "multipart/form-data",
                schema = @Schema(implementation = RecommendDTO.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "推薦更新成功"),
            @ApiResponse(responseCode = "400", description = "請求數據無效、店家ID不匹配或無權限操作"),
            @ApiResponse(responseCode = "401", description = "未經認證"),
            @ApiResponse(responseCode = "404", description = "未找到該推薦")
        }
    )
    @PutMapping("/{storeId}")
    public ResponseEntity<RecommendDTO> updateRecommendProcess(
            @Parameter(description = "URL 路徑中的店家ID", required = true, in = ParameterIn.PATH)
            @PathVariable("storeId") Integer pathStoreId,
            @Valid @ModelAttribute("recommendDTO") RecommendDTO recommendDTO,
            BindingResult bindingResult,
            Principal principal) {
        Integer currentUserId = getCurrentUserId(principal);
        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!pathStoreId.equals(recommendDTO.getStoreId())) {
            bindingResult.rejectValue("storeId", "error.recommendDTO", "無效的編輯請求，店家ID不匹配。");
        }

        // 手動驗證
        if (recommendDTO.getReason() == null || recommendDTO.getReason().trim().isEmpty()) {
            bindingResult.rejectValue("reason", "error.recommendDTO", "推薦原因不能為空！");
        }
        if (recommendDTO.getScore() == null || recommendDTO.getScore() < 1 || recommendDTO.getScore() > 5) {
            bindingResult.rejectValue("score", "error.recommendDTO", "請選擇有效的星級評分！");
        }

        if (bindingResult.hasErrors()) {
            logger.warn("編輯推薦表單驗證失敗。");
            return ResponseEntity.badRequest().body(recommendDTO);
        }

        try {
            if (recommendDTO.getStorePhoto() != null && !recommendDTO.getStorePhoto().isEmpty()) {
                String photoUrl = fileStorageService.uploadFile(recommendDTO.getStorePhoto());
                recommendDTO.setStorePhotoUrl(photoUrl);
            }
            UserModel currentUserModel = userService.findById(currentUserId)
                    .orElseThrow(() -> new EntityNotFoundException("當前用戶不存在或未找到。"));
            recommendService.updateRecommend(recommendDTO, currentUserModel);
            logger.info("用戶 {} 更新推薦成功，店家ID: {}。", currentUserModel.getEmail(), recommendDTO.getStoreId());
            return ResponseEntity.ok(recommendDTO);
        } catch (SecurityException | EntityNotFoundException e) {
            logger.error("更新推薦失敗：{}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            logger.error("更新推薦失敗：{}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    // endregion

    // region 刪除指定推薦
    @Operation(
        summary = "刪除指定推薦",
        description = "需認證。刪除當前登入用戶對指定店家的推薦。",
        parameters = {
            @Parameter(name = "storeId", description = "要刪除的店家ID", required = true, example = "101", in = ParameterIn.PATH)
        },
        responses = {
            @ApiResponse(responseCode = "204", description = "推薦成功刪除"),
            @ApiResponse(responseCode = "401", description = "未經認證"),
            @ApiResponse(responseCode = "404", description = "未找到該推薦或無權刪除")
        }
    )
    @DeleteMapping("/{storeId}")
    public ResponseEntity<Void> deleteRecommendProcess(
            @Parameter(description = "店家ID", required = true, in = ParameterIn.PATH)
            @PathVariable("storeId") Integer storeId,
            Principal principal) {
        Integer currentUserId = getCurrentUserId(principal);
        if (currentUserId == null) {
            logger.warn("未認證用戶嘗試刪除推薦，店家ID: {}", storeId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            UserModel currentUserModel = userService.findById(currentUserId)
                    .orElseThrow(() -> new EntityNotFoundException("當前用戶不存在或未找到。"));
            recommendService.deleteRecommend(currentUserId, storeId, currentUserModel);
            logger.info("用戶 ID: {} 成功刪除對店家 ID: {} 的推薦。", currentUserId, storeId);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            logger.error("刪除推薦失敗：找不到推薦或無權限。用戶ID: {}, 店家ID: {}. 錯誤訊息: {}", currentUserId, storeId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("刪除推薦時發生未知錯誤。用戶ID: {}, 店家ID: {}. 錯誤訊息: {}", currentUserId, storeId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    // endregion
}