package com.example.topfoodnow.service.category.impl;

import com.example.topfoodnow.controller.category.response.GetAllCategoriesData;
import com.example.topfoodnow.infra.category.Category;
import com.example.topfoodnow.infra.category.CategoryRepository;
import com.example.topfoodnow.service.category.CategoryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

  @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT,
          rollbackFor = Exception.class)
    public List<GetAllCategoriesData> getAllCategories() {
        return categoryRepository.findAllByOrderByIdAsc().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<GetAllCategoriesData> getCategoryById(Integer id) {
        return categoryRepository.findById(id).map(this::convertToDto);
    }

    @Transactional
    public GetAllCategoriesData createCategory(GetAllCategoriesData getAllCategoriesData) {
        if (categoryRepository.findByCategoryName(getAllCategoriesData.getCategoryName()).isPresent()) {
            throw new IllegalArgumentException("分類名稱已存在：" + getAllCategoriesData.getCategoryName());
        }
        Category category = convertToEntity(getAllCategoriesData);
        category.setId(null);
        return convertToDto(categoryRepository.save(category));
    }

    @Transactional
    public GetAllCategoriesData updateCategory(Integer id, GetAllCategoriesData getAllCategoriesData) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("分類未找到，ID: " + id));

        if (!existingCategory.getCategoryName().equals(getAllCategoriesData.getCategoryName()) &&
                categoryRepository.findByCategoryName(getAllCategoriesData.getCategoryName()).isPresent()) {
            throw new IllegalArgumentException("分類名稱已存在：" + getAllCategoriesData.getCategoryName());
        }

        existingCategory.setCategoryName(getAllCategoriesData.getCategoryName());
        return convertToDto(categoryRepository.save(existingCategory));
    }

    @Transactional
    public void deleteCategory(Integer id) {
        if (!categoryRepository.existsById(id)) {
            throw new EntityNotFoundException("分類未找到，ID: " + id);
        }
        categoryRepository.deleteById(id);
    }

    private GetAllCategoriesData convertToDto(Category model) {
        GetAllCategoriesData dto = new GetAllCategoriesData();
        dto.setId(model.getId());
        dto.setCategoryName(model.getCategoryName());
        return dto;
    }

    private Category convertToEntity(GetAllCategoriesData dto) {
        Category entity = new Category();
        entity.setId(dto.getId());
        entity.setCategoryName(dto.getCategoryName());
        return entity;
    }
}