package com.example.topfoodnow.infra.category;

import com.example.topfoodnow.infra.recommend.Recommend;
import com.example.topfoodnow.infra.store.Store;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "category")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "餐廳類別")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "餐廳類別ID", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer id;

    @Column(name = "name", nullable = false, unique = true)
    @Schema(name = "categoryName", description = "餐廳類別名稱", example = "中式料理")
    private String categoryName;

    // 多對多關聯到 RecommendModel
    @ManyToMany(mappedBy = "categories", fetch = FetchType.LAZY)
    @Schema(hidden = true)
    @EqualsAndHashCode.Exclude
    private Set<Recommend> recommends = new HashSet<>();

    @ManyToMany(mappedBy = "categories", fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    private Set<Store> stores = new HashSet<>();
}