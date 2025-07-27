package com.example.topfoodnow.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Set;
import java.util.HashSet;
import jakarta.persistence.*;

@Entity
@Table(name = "store")
@Data
@EqualsAndHashCode(exclude = {"categories", "recommends"}) // 避免無限遞歸
@Schema(description = "餐廳資料")
public class StoreModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "店家ID", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer id;

    @Column(name = "name", unique = true, nullable = false, length = 255)
    @Schema(description = "店家名稱", example = "某某咖啡店")
    private String name;

    @Column(name = "address", nullable = false, length = 255)
    @Schema(description = "店家地址", example = "台北市大安區忠孝東路三段2號")
    private String address;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY) // 延遲加載以提高性能
    @JoinTable(
        name = "store_category",
        joinColumns = @JoinColumn(name = "store_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<CategoryModel> categories = new HashSet<>();
}
