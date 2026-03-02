package com.example.topfoodnow.service.category.impl;

import com.example.topfoodnow.controller.category.request.DeleteCategoryRequest;
import com.example.topfoodnow.controller.category.response.DeleteCategoryResponse;
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
public class DeleteCategoryUseCase {

  private final CategoryRepository categoryRepository;

  @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT,
          rollbackFor = Exception.class)
  public DeleteCategoryResponse execute(DeleteCategoryRequest rq) {
    log.info(">>>> DeleteCategoryUseCase : Request = {}", rq);

    // 檢核分類是否存在
    if (!categoryRepository.existsById(rq.getId())) {
      log.error("分類未找到，ID: {}", rq.getId());
      throw new IllegalArgumentException("分類未找到，ID: " + rq.getId());
    }

    // 刪除分類
    categoryRepository.deleteById(rq.getId());

    log.info(">>>> DeleteCategoryUseCase : 成功刪除分類 ID = {}", rq.getId());
    return DeleteCategoryResponse.builder()
            .success(true)
            .message("分類已成功刪除")
            .build();
  }
}
