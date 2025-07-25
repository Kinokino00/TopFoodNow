package com.example.topfoodnow.controller;

import com.example.topfoodnow.dto.CategoryDTO;
import com.example.topfoodnow.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "分類管理")
public class CategoryController {
    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // region 取得所有店家分類
    @Operation(summary = "取得所有店家分類")
    @ApiResponse(responseCode = "200", description = "成功取得分類列表")
    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<CategoryDTO> categories = categoryService.getAllCategories();
        logger.info("成功取得所有分類");
        return ResponseEntity.ok(categories);
    }
    // endregion


    // region 根據ID取得分類
    @Operation(summary = "根據ID取得分類")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "成功取得分類"),
        @ApiResponse(responseCode = "404", description = "未找到分類")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Integer id) {
        return categoryService.getCategoryById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    logger.warn("未找到分類 ID: {}", id);
                    return ResponseEntity.notFound().build();
                });
    }
    // endregion


    // region 新增分類
    @Operation(summary = "新增分類", description = "創建一個新的店家分類。需要管理員權限")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "分類新增成功"),
        @ApiResponse(responseCode = "400", description = "請求數據無效或分類名稱已存在")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        try {
            CategoryDTO createdCategory = categoryService.createCategory(categoryDTO);
            logger.info("成功新增分類: {}", createdCategory.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
        } catch (IllegalArgumentException e) {
            logger.error("新增分類失敗: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }
    // endregion


    // region 更新分類
    @Operation(summary = "更新分類", description = "更新現有的店家分類。需要管理員權限")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "分類更新成功"),
        @ApiResponse(responseCode = "400", description = "請求數據無效或分類名稱已存在"),
        @ApiResponse(responseCode = "404", description = "未找到分類")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable Integer id, @Valid @RequestBody CategoryDTO categoryDTO) {
        try {
            CategoryDTO updatedCategory = categoryService.updateCategory(id, categoryDTO);
            logger.info("成功更新分類 ID: {}", id);
            return ResponseEntity.ok(updatedCategory);
        } catch (EntityNotFoundException e) {
            logger.warn("更新分類失敗: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            logger.error("更新分類失敗: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }
    // endregion


    // region 刪除分類
    @Operation(summary = "刪除分類", description = "根據ID刪除店家分類。需要管理員權限")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "分類成功刪除"),
        @ApiResponse(responseCode = "404", description = "未找到分類")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable Integer id) {
        try {
            categoryService.deleteCategory(id);
            logger.info("成功刪除分類 ID: {}", id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            logger.warn("刪除分類失敗: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    // endregion
}