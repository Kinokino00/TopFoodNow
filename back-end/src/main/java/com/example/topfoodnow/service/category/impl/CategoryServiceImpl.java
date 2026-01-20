package com.example.topfoodnow.service.category.impl;

import com.example.topfoodnow.controller.category.response.CategoryResponse;
import com.example.topfoodnow.infra.category.Category;
import com.example.topfoodnow.infra.category.CategoryRepository;
import com.example.topfoodnow.service.category.CategoryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAllByOrderByIdAsc().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CategoryResponse> getCategoryById(Integer id) {
        return categoryRepository.findById(id).map(this::convertToDto);
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryResponse categoryResponse) {
        if (categoryRepository.findByCategoryName(categoryResponse.getCategoryName()).isPresent()) {
            throw new IllegalArgumentException("分類名稱已存在：" + categoryResponse.getCategoryName());
        }
        Category category = convertToEntity(categoryResponse);
        category.setId(null);
        return convertToDto(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Integer id, CategoryResponse categoryResponse) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("分類未找到，ID: " + id));

        if (!existingCategory.getCategoryName().equals(categoryResponse.getCategoryName()) &&
                categoryRepository.findByCategoryName(categoryResponse.getCategoryName()).isPresent()) {
            throw new IllegalArgumentException("分類名稱已存在：" + categoryResponse.getCategoryName());
        }

        existingCategory.setCategoryName(categoryResponse.getCategoryName());
        return convertToDto(categoryRepository.save(existingCategory));
    }

    @Override
    @Transactional
    public void deleteCategory(Integer id) {
        if (!categoryRepository.existsById(id)) {
            throw new EntityNotFoundException("分類未找到，ID: " + id);
        }
        categoryRepository.deleteById(id);
    }

    private CategoryResponse convertToDto(Category model) {
        CategoryResponse dto = new CategoryResponse();
        dto.setId(model.getId());
        dto.setCategoryName(model.getCategoryName());
        return dto;
    }

    private Category convertToEntity(CategoryResponse dto) {
        Category entity = new Category();
        entity.setId(dto.getId());
        entity.setCategoryName(dto.getCategoryName());
        return entity;
    }
}