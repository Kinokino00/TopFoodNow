package com.example.topfoodnow.service.category.impl;

import com.example.topfoodnow.controller.category.request.GetAllCategoriesRequest;
import com.example.topfoodnow.controller.category.response.GetAllCategoriesResponse;
import com.example.topfoodnow.infra.category.Category;
import com.example.topfoodnow.infra.category.CategoryRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class GetAllCategoriesUseCase {

  private final CategoryRepository categoryRepository;

  @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT,
          rollbackFor = Exception.class)
  public GetAllCategoriesResponse execute(GetAllCategoriesRequest rq) {
    log.info(">>>> GetAllCategoriesUseCase : Request = {}", rq);
    List<Category> categoriesList = categoryRepository.findAllByOrderByIdAsc();

    return createResponse(categoriesList);
  }

  private GetAllCategoriesResponse createResponse(List<Category> categoriesList) {
    GetAllCategoriesResponse response = new GetAllCategoriesResponse();
    response.setGetAllCategoriesList(categoriesList);

    log.info(">>>> GetAllCategoriesUseCase : Response = {}", response);
    return response;
  }
}
