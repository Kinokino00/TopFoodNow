package com.example.topfoodnow.service.impl;

import com.example.topfoodnow.dto.CategoryDTO;
import com.example.topfoodnow.model.CategoryModel;
import com.example.topfoodnow.repository.CategoryRepository;
import com.example.topfoodnow.service.CategoryService;
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
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAllByOrderByIdAsc().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CategoryDTO> getCategoryById(Integer id) {
        return categoryRepository.findById(id).map(this::convertToDto);
    }

    @Override
    @Transactional
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        if (categoryRepository.findByCategoryName(categoryDTO.getCategoryName()).isPresent()) {
            throw new IllegalArgumentException("分類名稱已存在：" + categoryDTO.getCategoryName());
        }
        CategoryModel category = convertToEntity(categoryDTO);
        category.setId(null);
        return convertToDto(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryDTO updateCategory(Integer id, CategoryDTO categoryDTO) {
        CategoryModel existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("分類未找到，ID: " + id));

        if (!existingCategory.getCategoryName().equals(categoryDTO.getCategoryName()) &&
                categoryRepository.findByCategoryName(categoryDTO.getCategoryName()).isPresent()) {
            throw new IllegalArgumentException("分類名稱已存在：" + categoryDTO.getCategoryName());
        }

        existingCategory.setCategoryName(categoryDTO.getCategoryName());
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

    private CategoryDTO convertToDto(CategoryModel model) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(model.getId());
        dto.setCategoryName(model.getCategoryName());
        return dto;
    }

    private CategoryModel convertToEntity(CategoryDTO dto) {
        CategoryModel entity = new CategoryModel();
        entity.setId(dto.getId());
        entity.setCategoryName(dto.getCategoryName());
        return entity;
    }
}