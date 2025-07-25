package com.example.topfoodnow.repository;

import com.example.topfoodnow.model.CategoryModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryModel, Integer> {
    Optional<CategoryModel> findByName(String name);
}