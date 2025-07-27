package com.example.topfoodnow.controller;

import com.example.topfoodnow.model.UserModel;
import com.example.topfoodnow.dto.RecommendRequestDTO;
import com.example.topfoodnow.dto.RecommendResponseDTO;
import com.example.topfoodnow.dto.RecommendCreateRequestDTO;
import com.example.topfoodnow.service.RecommendService;
import com.example.topfoodnow.service.UserService;
import com.example.topfoodnow.service.GcsService;
import com.example.topfoodnow.dto.CustomPageResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private ObjectMapper objectMapper;

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
            logger.warn("getCurrentUserId: UserService 找不到對應於郵箱 {} 的用戶。這可能表示數據不一致", principalName);
            return null;
        }
    }


    // region 所有餐廳推薦
    @Operation(summary = "所有餐廳推薦")
    @ApiResponse(responseCode = "200", description = "成功取得分頁推薦列表")
    @GetMapping("/all")
    public ResponseEntity<CustomPageResponseDTO<RecommendResponseDTO>> getAllRecommends(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort
    ) {
        logger.info("請求取得所有餐廳推薦，頁碼: {}, 大小: {}, 排序: {}", page, size, sort);
        int springPage = page - 1;
        if (springPage < 0) {
            springPage = 0;
        }

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

        Pageable pageable = PageRequest.of(springPage, size, springSort);

        // 這裡調用 service 層獲取 Page<RecommendResponseDTO>
        Page<RecommendResponseDTO> recommendPage = recommendService.findAllRecommendsPaged(pageable.getPageNumber(), pageable.getPageSize(), sort);

        CustomPageResponseDTO<RecommendResponseDTO> response = new CustomPageResponseDTO<>();
        response.setData(recommendPage.getContent()); // <-- 將 setContent 改為 setData
        response.setTotalElements(recommendPage.getTotalElements());
        response.setTotalPages(recommendPage.getTotalPages());

        CustomPageResponseDTO.CustomPageableInfo pageableInfo = new CustomPageResponseDTO.CustomPageableInfo();
        pageableInfo.setPageNumber(recommendPage.getNumber() + 1);
        pageableInfo.setPageSize(recommendPage.getSize());
        pageableInfo.setSort(springSort.isSorted() ? springSort.toString() : "unsorted");

        response.setPageable(pageableInfo);
        return ResponseEntity.ok(response);
    }
    // endregion
    
    // region 指定用戶的所有個人推薦
    @Operation(summary = "指定用戶的所有個人推薦", description = "無需認證。返回指定用戶的所有餐廳推薦列表")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功取得指定用戶的推薦列表"),
            @ApiResponse(responseCode = "404", description = "未找到指定用戶")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RecommendResponseDTO>> getRecommendsBySpecificUserId(
            @Parameter(description = "用戶 ID", required = true, example = "1", in = ParameterIn.PATH)
            @PathVariable Integer userId) {
        if (!userService.findById(userId).isPresent()) {
            logger.warn("嘗試取得推薦：用戶 ID: {} 不存在", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        List<RecommendResponseDTO> recommends = recommendService.getRecommendsByUserId(userId);
        logger.info("成功取得用戶 ID: {} 的所有推薦列表", userId);
        return ResponseEntity.ok(recommends);
    }
    // endregion

    // region 指定用戶對特定店家的推薦詳情
    @Operation(summary = "指定用戶對特定店家的推薦詳情", description = "無需認證。返回指定用戶對指定店家的推薦詳細資訊。可用於公開分享")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "成功取得推薦詳情"),
            @ApiResponse(responseCode = "404", description = "未找到該推薦")
    })
    @GetMapping("/{userId}/{storeId}")
    public ResponseEntity<RecommendResponseDTO> getSpecificUserRecommendDetail(
            @Parameter(description = "用戶 ID", required = true, example = "1", in = ParameterIn.PATH)
            @PathVariable Integer userId,
            @Parameter(description = "店家 ID", required = true, example = "1", in = ParameterIn.PATH)
            @PathVariable Integer storeId) {
        logger.info("嘗試查看指定用戶 ID: {} 對店家 ID: {} 的推薦詳情", userId, storeId);

        Optional<RecommendResponseDTO> recommendOptional = recommendService.getRecommendByUserAndStoreId(userId, storeId);
        if (recommendOptional.isPresent()) {
            logger.info("找到推薦，返回詳情");
            return ResponseEntity.ok(recommendOptional.get());
        } else {
            logger.warn("未找到用戶 ID: {} 對店家 ID: {} 的推薦，返回 404", userId, storeId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    // endregion

    // region 新增餐廳推薦
    @Operation(
            summary = "新增餐廳推薦",
            description = "需認證。接收當前認證用戶提交的餐廳推薦信息（包含店家詳情和推薦原因/評分，可選上傳圖片）",
            requestBody = @RequestBody(
                    description = "推薦信息和可選的店家圖片。推薦信息通過表單字段提交，圖片文件也通過表單字段提交。",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = RecommendCreateRequestDTO.class)
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
    public ResponseEntity<RecommendResponseDTO> addRecommendProcess(
            @ModelAttribute RecommendCreateRequestDTO recommendCreateRequestDTO,
            Principal principal) {
        Integer currentUserId = getCurrentUserId(principal);
        if (currentUserId == null) {
            logger.warn("未認證用戶嘗試新增推薦");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 手動驗證
        List<String> errors = new ArrayList<>();
        if (recommendCreateRequestDTO.getStoreName() == null || recommendCreateRequestDTO.getStoreName().trim().isEmpty()) {
            errors.add("店家名稱為必填！");
        }
        if (recommendCreateRequestDTO.getStoreAddress() == null || recommendCreateRequestDTO.getStoreAddress().trim().isEmpty()) {
            errors.add("店家地址為必填！");
        }
        if (recommendCreateRequestDTO.getReason() == null || recommendCreateRequestDTO.getReason().trim().isEmpty()) {
            errors.add("推薦原因不能為空！");
        }
        if (recommendCreateRequestDTO.getScore() == null || recommendCreateRequestDTO.getScore() < 1 || recommendCreateRequestDTO.getScore() > 5) {
            errors.add("請選擇有效的星級評分 (1-5)！");
        }
        // 圖片驗證
        if (recommendCreateRequestDTO.getStorePhoto() == null || recommendCreateRequestDTO.getStorePhoto().isEmpty() || recommendCreateRequestDTO.getStorePhoto().stream().allMatch(MultipartFile::isEmpty)) {
            errors.add("請上傳店家圖片！"); // 如果圖片是必填項
        } else if (recommendCreateRequestDTO.getStorePhoto().size() > 5) { // 實際限制上傳數量
            errors.add("最多只能上傳 5 張圖片！");
        }

        if (!errors.isEmpty()) {
            logger.warn("新增推薦表單驗證失敗：{}", errors);
            return ResponseEntity.badRequest().body(null);
        }

        try {
            // 圖片上傳
            List<String> uploadedPhotoUrls = new ArrayList<>();
            if (recommendCreateRequestDTO.getStorePhoto() != null && !recommendCreateRequestDTO.getStorePhoto().isEmpty()) {
                for (MultipartFile file : recommendCreateRequestDTO.getStorePhoto()) {
                    if (!file.isEmpty()) {
                        String photoUrl = gcsService.uploadFile(file, "recommend-images/");
                        uploadedPhotoUrls.add(photoUrl);
                    }
                }
            }

            UserModel currentUserModel = userService.findById(currentUserId)
                    .orElseThrow(() -> new EntityNotFoundException("當前用戶不存在或未找到"));

            // 調用服務層，將上傳的 URL 列表傳遞給 Service
            RecommendResponseDTO createdRecommend = recommendService.addRecommend(recommendCreateRequestDTO, uploadedPhotoUrls, currentUserModel);
            logger.info("用戶 {} 新增推薦成功", currentUserModel.getEmail());

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

    // region 更新指定推薦
    @Operation(
            summary = "更新指定推薦",
            description = "需認證。更新當前登入用戶對指定店家的推薦",
            parameters = {
                    @Parameter(name = "storeId", description = "要更新的店家ID", required = true, example = "101", in = ParameterIn.PATH)
            },
            requestBody = @RequestBody(
                    description = "更新後的推薦信息和可選的店家圖片。圖片通過 multipart/form-data 提交",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = RecommendRequestDTO.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "推薦更新成功"),
                    @ApiResponse(responseCode = "400", description = "請求數據無效、店家ID不匹配或無權限操作"),
                    @ApiResponse(responseCode = "401", description = "未經認證"),
                    @ApiResponse(responseCode = "404", description = "未找到該推薦")
            }
    )
    @PutMapping(value = "/{storeId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RecommendResponseDTO> updateRecommendProcess(
            @Parameter(description = "URL 路徑中的店家ID", required = true, in = ParameterIn.PATH)
            @PathVariable("storeId") Integer pathStoreId,
            @ModelAttribute RecommendRequestDTO recommendRequestDTO,
            @RequestPart(value = "storePhoto", required = false) List<MultipartFile> storePhotoFiles, // <-- 關鍵改動：明確接收 MultipartFile 列表
            Principal principal) {
        Integer currentUserId = getCurrentUserId(principal);
        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<String> errors = new ArrayList<>();
        if (recommendRequestDTO.getStoreId() == null || !pathStoreId.equals(recommendRequestDTO.getStoreId())) {
            errors.add("店家ID不匹配或缺失。");
        }
        if (recommendRequestDTO.getStoreName() == null || recommendRequestDTO.getStoreName().trim().isEmpty()) {
            errors.add("店家名稱為必填！");
        }
        if (recommendRequestDTO.getStoreAddress() == null || recommendRequestDTO.getStoreAddress().trim().isEmpty()) {
            errors.add("店家地址為必填！");
        }
        if (recommendRequestDTO.getReason() == null || recommendRequestDTO.getReason().trim().isEmpty()) {
            errors.add("推薦原因不能為空！");
        }
        if (recommendRequestDTO.getScore() == null || recommendRequestDTO.getScore() < 1 || recommendRequestDTO.getScore() > 5) {
            errors.add("請選擇有效的星級評分 (1-5)！");
        }
        if (storePhotoFiles != null && !storePhotoFiles.isEmpty()) {
            long nonNullFilesCount = storePhotoFiles.stream().filter(file -> !file.isEmpty()).count();
            if (nonNullFilesCount > 5) {
                errors.add("最多只能上傳 5 張圖片！");
            }
        }

        if (!errors.isEmpty()) {
            logger.warn("編輯推薦表單驗證失敗：{}", errors);
            return ResponseEntity.badRequest().body(null);
        }

        try {
            // 圖片上傳
            List<String> newUploadedPhotoUrls = new ArrayList<>();
            if (storePhotoFiles != null && !storePhotoFiles.isEmpty()) {
                for (MultipartFile file : storePhotoFiles) {
                    if (!file.isEmpty()) { // 檢查文件是否真的有內容
                        String photoUrl = gcsService.uploadFile(file, "recommend-images/");
                        newUploadedPhotoUrls.add(photoUrl);
                    }
                }
            }

            UserModel currentUserModel = userService.findById(currentUserId)
                    .orElseThrow(() -> new EntityNotFoundException("當前用戶不存在或未找到"));

            RecommendResponseDTO updatedRecommend = recommendService.updateRecommend(recommendRequestDTO, newUploadedPhotoUrls, currentUserModel);
            logger.info("用戶 {} 更新推薦成功，店家ID: {}", currentUserModel.getEmail(), recommendRequestDTO.getStoreId());

            return ResponseEntity.ok(updatedRecommend);
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            logger.error("更新推薦失敗：{}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (IOException e) {
            logger.error("更新推薦失敗：圖片上傳錯誤：{}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } catch (Exception e) {
            logger.error("更新推薦失敗：意外錯誤：{}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    // endregion

    // region 刪除指定推薦
    @Operation(
            summary = "刪除指定推薦",
            description = "需認證。刪除當前登入用戶對指定店家的推薦",
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
                    .orElseThrow(() -> new EntityNotFoundException("當前用戶不存在或未找到"));
            recommendService.deleteRecommend(currentUserId, storeId, currentUserModel);
            logger.info("用戶 ID: {} 成功刪除對店家 ID: {} 的推薦。", currentUserId, storeId);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            logger.error("刪除推薦失敗：找不到推薦或無權限。用戶ID: {}, 店家ID: {}. 錯誤訊息: {}", currentUserId, storeId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("刪除推薦失敗：內部伺服器錯誤。用戶ID: {}, 店家ID: {}. 錯誤訊息: {}", currentUserId, storeId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    // endregion
}