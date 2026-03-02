package com.example.topfoodnow.service.category.impl;

import com.example.topfoodnow.controller.category.request.UpdateCategoryRequest;
import com.example.topfoodnow.controller.category.response.UpdateCategoryResponse;
import com.example.topfoodnow.infra.category.Category;
import com.example.topfoodnow.infra.category.CategoryRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@AllArgsConstructor
public class UpdateCategoryUseCase {

  private final CategoryRepository categoryRepository;

  @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT,
          rollbackFor = Exception.class)
  public UpdateCategoryResponse execute(UpdateCategoryRequest rq) {
    log.info(">>>> UpdateCategoryUseCase : Request = {}", rq);

    // 取得分類
    Category category = categoryRepository.findById(rq.getId())
            .orElseThrow(() -> new IllegalArgumentException("分類未找到，ID: " + rq.getId()));

    // 檢核分類名稱是否已存在（排除自己）
    if (!category.getCategoryName().equals(rq.getCategoryName()) &&
            categoryRepository.findByCategoryName(rq.getCategoryName()).isPresent()) {
      log.error("分類名稱已存在: {}", rq.getCategoryName());
      throw new IllegalArgumentException("分類名稱已存在：" + rq.getCategoryName());
    }

    // 更新分類
    category.setCategoryName(rq.getCategoryName());
    Category updatedCategory = categoryRepository.save(category);

    return createResponse(updatedCategory);
  }

  private UpdateCategoryResponse createResponse(Category category) {
    UpdateCategoryResponse response = new UpdateCategoryResponse();
    response.setId(category.getId());
    response.setCategoryName(category.getCategoryName());

    log.info(">>>> UpdateCategoryUseCase : Response = {}", response);
    return response;
  }
}
