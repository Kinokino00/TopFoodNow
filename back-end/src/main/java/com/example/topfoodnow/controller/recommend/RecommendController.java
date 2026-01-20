package com.example.topfoodnow.controller.recommend;

import com.example.topfoodnow.infra.user.User;
import com.example.topfoodnow.controller.recommend.request.RecommendRequest;
import com.example.topfoodnow.controller.recommend.response.RecommendResponse;
import com.example.topfoodnow.controller.recommend.request.CreateRecommendRequest;
import com.example.topfoodnow.service.recommend.RecommendService;
import com.example.topfoodnow.service.user.UserService;
import com.example.topfoodnow.service.gcs.GcsService;
import com.example.topfoodnow.controller.recommend.response.CustomPageResponse;
import com.example.topfoodnow.infra.store.StoreRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.persistence.EntityNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.security.Principal;

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
    private GcsService gcsService;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Integer getCurrentUserId(Principal principal) {
        if (principal == null) {
            logger.debug("getCurrentUserId: Principal is null, user not logged in.");
            return null;
        }
        String principalName = principal.getName();
        Optional<User> userOptional = userService.findByEmail(principalName);
        if (userOptional.isPresent()) {
            logger.debug("getCurrentUserId: Found UserModel for email {}, ID: {}", principalName, userOptional.get().getId());
            return userOptional.get().getId();
        } else {
            logger.warn("getCurrentUserId: UserService 找不到對應於郵箱 {} 的用戶。這可能表示數據不一致", principalName);
            return null;
        }
    }


    // region 所有餐廳推薦
    @Operation(summary = "所有餐廳推薦")
    @ApiResponse(responseCode = "200", description = "成功取得分頁推薦列表")
    @GetMapping("/all")
    public ResponseEntity<CustomPageResponse<RecommendResponse>> getAllRecommends(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "asc") String sortOrder,
        @RequestParam(required = false) String searchTerm
    ) {
        logger.info("請求取得所有餐廳推薦，頁碼: {}, 大小: {}, 排序欄位: {}, 排序方式: {}", page, size, sortBy, sortOrder);
        int springPage = page - 1;
        if (springPage < 0) {
            springPage = 0;
        }

        Sort springSort;
        try {
            Sort.Direction direction = Sort.Direction.fromString(sortOrder.toUpperCase());
            springSort = Sort.by(direction, sortBy);
        } catch (IllegalArgumentException e) {
            logger.warn("解析排序參數失敗，使用預設排序 (id, asc)。錯誤: {}", e.getMessage());
            springSort = Sort.by(Sort.Direction.ASC, "id");
        }

        Pageable pageable = PageRequest.of(springPage, size, springSort);

        Page<RecommendResponse> recommendPage = recommendService.findAllRecommendsPaged(pageable, searchTerm);

        CustomPageResponse<RecommendResponse> response = new CustomPageResponse<>();
        response.setData(recommendPage.getContent());
        response.setTotalElements(recommendPage.getTotalElements());
        response.setTotalPages(recommendPage.getTotalPages());

        CustomPageResponse.CustomPageableInfo pageableInfo = new CustomPageResponse.CustomPageableInfo();
        pageableInfo.setPageNumber(recommendPage.getNumber() + 1);
        pageableInfo.setPageSize(recommendPage.getSize());

        if (springSort.isSorted()) {
            Sort.Order order = springSort.iterator().next();
            pageableInfo.setSortBy(order.getProperty());
            pageableInfo.setSortOrder(order.getDirection().name());
        } else {
            pageableInfo.setSortBy("id");
            pageableInfo.setSortOrder("ASC");
        }

        response.setPageable(pageableInfo);
        return ResponseEntity.ok(response);
    }
    // endregion

    // region 取得指定店家的所有推薦 (無需認證)
    @Operation(summary = "取得指定店家的所有推薦 (無需認證)", description = "根據店家ID取得該店家的所有餐廳推薦列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功取得指定店家的推薦列表"),
        @ApiResponse(responseCode = "404", description = "未找到指定店家")
    })
    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<RecommendResponse>> getRecommendsByStoreId(
            @Parameter(description = "店家 ID", required = true, example = "1", in = ParameterIn.PATH)
            @PathVariable Integer storeId) {
        logger.info("請求取得店家 ID: {} 的所有推薦", storeId);
        // 先檢查店家是否存在
        if (!storeRepository.existsById(storeId)) {
            logger.warn("嘗試取得推薦：店家 ID: {} 不存在", storeId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        List<RecommendResponse> recommends = recommendService.getRecommendsByStoreId(storeId);
        logger.info("成功取得店家 ID: {} 的所有推薦列表，共 {} 筆", storeId, recommends.size());
        return ResponseEntity.ok(recommends);
    }
    // endregion

    // region 指定用戶的所有個人推薦 (無需認證)
    @Operation(summary = "指定用戶的所有個人推薦 (無需認證)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功取得指定用戶的推薦列表"),
        @ApiResponse(responseCode = "404", description = "未找到指定用戶")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RecommendResponse>> getRecommendsBySpecificUserId(
            @Parameter(description = "用戶 ID", required = true, example = "1", in = ParameterIn.PATH)
            @PathVariable Integer userId) {
        if (!userService.findById(userId).isPresent()) {
            logger.warn("嘗試取得推薦：用戶 ID: {} 不存在", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        List<RecommendResponse> recommends = recommendService.getRecommendsByUserId(userId);
        logger.info("成功取得用戶 ID: {} 的所有推薦列表", userId);
        return ResponseEntity.ok(recommends);
    }
    // endregion

    // region 指定用戶對特定店家的推薦詳情 (無需認證)
    @Operation(summary = "指定用戶對特定店家的推薦詳情 (無需認證)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功取得推薦詳情"),
        @ApiResponse(responseCode = "404", description = "未找到該推薦")
    })
    @GetMapping("/{userId}/{storeId}")
    public ResponseEntity<RecommendResponse> getSpecificUserRecommendDetail(
            @Parameter(description = "用戶 ID", required = true, example = "1", in = ParameterIn.PATH)
            @PathVariable Integer userId,
            @Parameter(description = "店家 ID", required = true, example = "1", in = ParameterIn.PATH)
            @PathVariable Integer storeId) {
        logger.info("嘗試查看指定用戶 ID: {} 對店家 ID: {} 的推薦詳情", userId, storeId);

        Optional<RecommendResponse> recommendOptional = recommendService.getRecommendByUserAndStoreId(userId, storeId);
        if (recommendOptional.isPresent()) {
            logger.info("找到推薦，返回詳情");
            return ResponseEntity.ok(recommendOptional.get());
        } else {
            logger.warn("未找到用戶 ID: {} 對店家 ID: {} 的推薦，返回 404", userId, storeId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    // endregion

    // region 新增當前認證用戶提交的餐廳推薦
    @Operation(
        summary = "新增當前認證用戶提交的餐廳推薦",
        requestBody = @RequestBody(
            required = true,
            content = @Content(
                mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                schema = @Schema(implementation = CreateRecommendRequest.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "201", description = "推薦新增成功"),
            @ApiResponse(responseCode = "400", description = "請求數據無效或缺少必要字段"),
            @ApiResponse(responseCode = "401", description = "未經認證"),
            @ApiResponse(responseCode = "500", description = "伺服器內部錯誤")
        }
    )
    @PostMapping(value = "/add-recommend", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RecommendResponse> addRecommendProcess(
            @ModelAttribute CreateRecommendRequest createRecommendRequest,
            Principal principal) {
        Integer currentUserId = getCurrentUserId(principal);
        if (currentUserId == null) {
            logger.warn("未認證用戶嘗試新增推薦");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 手動驗證
        List<String> errors = new ArrayList<>();
        if (createRecommendRequest.getStoreName() == null || createRecommendRequest.getStoreName().trim().isEmpty()) {
            errors.add("店家名稱為必填！");
        }
        if (createRecommendRequest.getStoreAddress() == null || createRecommendRequest.getStoreAddress().trim().isEmpty()) {
            errors.add("店家地址為必填！");
        }
        if (createRecommendRequest.getReason() == null || createRecommendRequest.getReason().trim().isEmpty()) {
            errors.add("推薦原因不能為空！");
        }
        if (createRecommendRequest.getScore() == null || createRecommendRequest.getScore() < 1 || createRecommendRequest.getScore() > 5) {
            errors.add("請選擇有效的星級評分 (1-5)！");
        }
        // 圖片驗證
        if (createRecommendRequest.getStorePhoto() == null || createRecommendRequest.getStorePhoto().isEmpty() || createRecommendRequest.getStorePhoto().stream().allMatch(MultipartFile::isEmpty)) {
            errors.add("請上傳店家圖片！");
        } else if (createRecommendRequest.getStorePhoto().size() > 5) {
            errors.add("最多只能上傳 5 張圖片！");
        }

        if (!errors.isEmpty()) {
            logger.warn("新增推薦表單驗證失敗：{}", errors);
            return ResponseEntity.badRequest().body(null);
        }

        try {
            // 圖片上傳
            List<String> uploadedPhotoUrls = new ArrayList<>();
            if (createRecommendRequest.getStorePhoto() != null && !createRecommendRequest.getStorePhoto().isEmpty()) {
                for (MultipartFile file : createRecommendRequest.getStorePhoto()) {
                    if (!file.isEmpty()) {
                        String photoUrl = gcsService.uploadFile(file, "recommend-images/");
                        uploadedPhotoUrls.add(photoUrl);
                    }
                }
            }

            User currentUser = userService.findById(currentUserId)
                    .orElseThrow(() -> new EntityNotFoundException("當前用戶不存在或未找到"));

            // 調用服務層，將上傳的 URL 列表傳遞給 Service
            RecommendResponse createdRecommend = recommendService.addRecommend(createRecommendRequest, uploadedPhotoUrls, currentUser);
            logger.info("用戶 {} 新增推薦成功", currentUser.getEmail());

            return ResponseEntity.status(HttpStatus.CREATED).body(createdRecommend);
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            logger.error("新增推薦失敗：{}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        } catch (IOException e) {
            logger.error("新增推薦失敗：圖片上傳錯誤：{}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } catch (Exception e) {
            logger.error("新增推薦失敗：意外錯誤：{}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    // endregion


    // region 更新當前認證用戶對指定店家的推薦
    @Operation(
        summary = "更新當前認證用戶對指定推薦",
        parameters = {
            @Parameter(name = "recommendId", description = "要更新的推薦的ID", required = true, example = "1", in = ParameterIn.PATH)
        },
        requestBody = @RequestBody(
            description = "更新後的推薦信息和可選的店家圖片。圖片通過 multipart/form-oata 提交",
            required = true,
            content = @Content(
                mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                schema = @Schema(implementation = RecommendRequest.class)
            )
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "推薦更新成功"),
            @ApiResponse(responseCode = "400", description = "請求數據無效或無權限操作"),
            @ApiResponse(responseCode = "401", description = "未經認證"),
            @ApiResponse(responseCode = "404", description = "未找到該推薦")
        }
    )
    @PutMapping(value = "/{recommendId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RecommendResponse> updateRecommendProcess(
            @Parameter(description = "URL 路徑中的推薦ID", required = true, in = ParameterIn.PATH)
            @PathVariable("recommendId") Integer recommendId,
            @ModelAttribute RecommendRequest recommendRequest,
            @Parameter(description = "希望保留的現有圖片URL的JSON字符串", example = "[\"url1\",\"url2\"]")
            @RequestParam(value = "retainedPhotoUrls", required = false) String retainedPhotoUrlsJson,
            Principal principal) {
        Integer currentUserId = getCurrentUserId(principal);
        if (currentUserId == null) {
            logger.warn("未認證用戶嘗試更新推薦");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<String> errors = new ArrayList<>();
        if (recommendRequest.getStoreName() == null || recommendRequest.getStoreName().trim().isEmpty()) {
            errors.add("店家名稱為必填！");
        }
        if (recommendRequest.getStoreAddress() == null || recommendRequest.getStoreAddress().trim().isEmpty()) {
            errors.add("店家地址為必填！");
        }
        if (recommendRequest.getReason() == null || recommendRequest.getReason().trim().isEmpty()) {
            errors.add("推薦原因不能為空！");
        }
        if (recommendRequest.getScore() == null || recommendRequest.getScore() < 1 || recommendRequest.getScore() > 5) {
            errors.add("請選擇有效的星級評分 (1-5)！");
        }
        // 圖片驗證 (只驗證新上傳的圖片數量)
        if (recommendRequest.getStorePhoto() != null && !recommendRequest.getStorePhoto().isEmpty()) {
            long nonNullFilesCount = recommendRequest.getStorePhoto().stream().filter(file -> !file.isEmpty()).count();
            if (nonNullFilesCount > 5) {
                errors.add("單次最多只能上傳 5 張圖片！");
            }
        }

        List<String> retainedPhotoUrls = new ArrayList<>();
        if (retainedPhotoUrlsJson != null && !retainedPhotoUrlsJson.trim().isEmpty()) {
            try {
                // 手動使用 ObjectMapper 解析 JSON 字符串為 List<String>
                retainedPhotoUrls = objectMapper.readValue(retainedPhotoUrlsJson, new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});
            } catch (JsonProcessingException e) {
                logger.error("解析 retainedPhotoUrls JSON 字符串失敗: {}", retainedPhotoUrlsJson, e);
                return ResponseEntity.badRequest().body(null);
            }
        }

        if (!errors.isEmpty()) {
            logger.warn("更新推薦表單驗證失敗：{}", errors);
            return ResponseEntity.badRequest().body(null);
        }

        try {
            // 只處理新上傳的圖片
            List<String> newUploadedPhotoUrls = new ArrayList<>();
            if (recommendRequest.getStorePhoto() != null && !recommendRequest.getStorePhoto().isEmpty()) {
                for (MultipartFile file : recommendRequest.getStorePhoto()) {
                    if (!file.isEmpty()) {
                        String photoUrl = gcsService.uploadFile(file, "recommend-images/");
                        newUploadedPhotoUrls.add(photoUrl);
                    }
                }
            }

            User currentUser = userService.findById(currentUserId)
                    .orElseThrow(() -> new EntityNotFoundException("當前用戶不存在或未找到"));

            RecommendResponse updatedRecommend = recommendService.updateRecommend(
                    recommendId,
                    recommendRequest,
                    newUploadedPhotoUrls,
                    retainedPhotoUrls,
                    currentUser
            );
            logger.info("用戶 {} 更新推薦成功，推薦ID: {}", currentUser.getEmail(), recommendRequest.getId());

            return ResponseEntity.ok(updatedRecommend);
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            logger.error("更新推薦失敗：{}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (IOException e) {
            logger.error("更新推薦失敗：圖片上傳/刪除錯誤：{}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } catch (Exception e) {
            logger.error("更新推薦失敗：意外錯誤：{}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    // endregion

    // region 刪除當前認證用戶對指定店家的推薦
    @Operation(
        summary = "刪除當前認證用戶對指定店家的推薦",
        parameters = {
            @Parameter(name = "storeId", description = "欲刪除的店家ID", required = true, example = "101", in = ParameterIn.PATH)
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
            User currentUser = userService.findById(currentUserId)
                    .orElseThrow(() -> new EntityNotFoundException("當前用戶不存在或未找到"));
            recommendService.deleteRecommend(currentUserId, storeId, currentUser);
            logger.info("用戶 ID: {} 成功刪除對店家 ID: {} 的推薦。", currentUserId, storeId);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            logger.error("刪除推薦失敗：找不到推薦或無權限。用戶ID: {}, 店家ID: {}. 錯誤訊息: {}", currentUserId, storeId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IOException e) {
            logger.error("刪除推薦失敗：圖片刪除錯誤。用戶ID: {}, 店家ID: {}. 錯誤訊息: {}", currentUserId, storeId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            logger.error("刪除推薦失敗：意外錯誤。用戶ID: {}, 店家ID: {}. 錯誤訊息: {}", currentUserId, storeId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    // endregion
}