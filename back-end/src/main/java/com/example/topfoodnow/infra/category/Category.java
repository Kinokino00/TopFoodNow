package com.example.topfoodnow.infra.category;

import com.example.topfoodnow.infra.recommend.Recommend;
import com.example.topfoodnow.infra.store.Store;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

import java.util.Set;
import java.util.HashSet;

@Getter
@Setter
@DynamicUpdate
@Entity
// TODO table名改大駝峰
@Table(name = "category", schema = "dbo")
public class Category {
    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "name")
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