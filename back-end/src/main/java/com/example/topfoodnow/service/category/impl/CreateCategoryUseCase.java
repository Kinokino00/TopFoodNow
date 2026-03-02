package com.example.topfoodnow.service.category.impl;

import com.example.topfoodnow.controller.category.request.CreateCategoryRequest;
import com.example.topfoodnow.controller.category.response.CreateCategoryResponse;
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
public class CreateCategoryUseCase {

  private final CategoryRepository categoryRepository;

  @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT,
          rollbackFor = Exception.class)
  public CreateCategoryResponse execute(CreateCategoryRequest rq) {
    log.info(">>>> CreateCategoryUseCase : Request = {}", rq);

    // 檢核分類名稱是否已存在
    if (categoryRepository.findByCategoryName(rq.getCategoryName()).isPresent()) {
      log.error("分類名稱已存在: {}", rq.getCategoryName());
      throw new IllegalArgumentException("分類名稱已存在：" + rq.getCategoryName());
    }

    // 建立新分類
    Category category = new Category();
    category.setCategoryName(rq.getCategoryName());
    Category savedCategory = categoryRepository.save(category);

    return createResponse(savedCategory);
  }

  private CreateCategoryResponse createResponse(Category category) {
    CreateCategoryResponse response = new CreateCategoryResponse();
    response.setId(category.getId());
    response.setCategoryName(category.getCategoryName());

    log.info(">>>> CreateCategoryUseCase : Response = {}", response);
    return response;
  }
}
