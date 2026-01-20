package com.example.topfoodnow.service.category;

import com.example.topfoodnow.controller.category.response.CategoryResponse;
import java.util.List;
import java.util.Optional;

public interface CategoryService {
    List<CategoryResponse> getAllCategories();
    Optional<CategoryResponse> getCategoryById(Integer id);
    CategoryResponse createCategory(CategoryResponse categoryResponse);
    CategoryResponse updateCategory(Integer id, CategoryResponse categoryResponse);
    void deleteCategory(Integer id);
}