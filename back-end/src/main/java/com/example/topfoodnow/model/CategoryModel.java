package com.example.topfoodnow.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "category")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "餐廳類別")
public class CategoryModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "餐廳類別ID", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String name;

    // 多對多關聯到 RecommendModel
    @ManyToMany(mappedBy = "categories", fetch = FetchType.LAZY)
    @Schema(hidden = true)
    private Set<RecommendModel> recommends = new HashSet<>();

    @ManyToMany(mappedBy = "categories", fetch = FetchType.LAZY)
    private Set<StoreModel> stores = new HashSet<>();
}