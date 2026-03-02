package com.example.topfoodnow.controller.category;

import com.example.topfoodnow.controller.category.request.GetAllCategoriesRequest;
import com.example.topfoodnow.controller.category.request.CreateCategoryRequest;
import com.example.topfoodnow.controller.category.request.UpdateCategoryRequest;
import com.example.topfoodnow.controller.category.request.DeleteCategoryRequest;
import com.example.topfoodnow.controller.category.response.GetAllCategoriesResponse;
import com.example.topfoodnow.controller.category.response.CreateCategoryResponse;
import com.example.topfoodnow.controller.category.response.UpdateCategoryResponse;
import com.example.topfoodnow.controller.category.response.DeleteCategoryResponse;
import com.example.topfoodnow.service.category.impl.GetAllCategoriesUseCase;
import com.example.topfoodnow.service.category.impl.CreateCategoryUseCase;
import com.example.topfoodnow.service.category.impl.UpdateCategoryUseCase;
import com.example.topfoodnow.service.category.impl.DeleteCategoryUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Slf4j
@RestController
@AllArgsConstructor
@Validated
@RequestMapping("/frontend")
@Tag(name = "分類管理")
public class CategoryController {

  private final GetAllCategoriesUseCase getAllCategoriesUseCase;
  private final CreateCategoryUseCase createCategoryUseCase;
  private final UpdateCategoryUseCase updateCategoryUseCase;
  private final DeleteCategoryUseCase deleteCategoryUseCase;

  @Operation(summary = "取得所有分類")
  @PostMapping("/categories")
  public ResponseEntity<GetAllCategoriesResponse> getAllCategories(
      @RequestBody @Valid GetAllCategoriesRequest rq) {
    return ResponseEntity.ok(getAllCategoriesUseCase.execute(rq));
  }

  @Operation(summary = "新增分類", description = "創建一個新的店家分類。需要管理員權限")
  @PostMapping("/createCategory")
  public ResponseEntity<CreateCategoryResponse> createCategory(
      @RequestBody @Valid CreateCategoryRequest rq) {
    return ResponseEntity.ok(createCategoryUseCase.execute(rq));
  }
  
  @Operation(summary = "更新分類", description = "更新現有的店家分類。需要管理員權限")
  @PostMapping("/updateCategory")
  public ResponseEntity<UpdateCategoryResponse> updateCategory(
      @RequestBody @Valid UpdateCategoryRequest rq) {
    return ResponseEntity.ok(updateCategoryUseCase.execute(rq));
  }
  
  @Operation(summary = "刪除分類", description = "根據ID刪除店家分類。需要管理員權限")
  @PostMapping("/deleteCategory")
  public ResponseEntity<DeleteCategoryResponse> deleteCategory(
      @RequestBody @Valid DeleteCategoryRequest rq) {
    return ResponseEntity.ok(deleteCategoryUseCase.execute(rq));
  }
}