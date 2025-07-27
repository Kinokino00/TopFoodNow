package com.example.topfoodnow.model;

import com.example.topfoodnow.converter.StringListToJsonConverter;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;
import java.util.List;

@Entity
@Table(name = "recommend")
@Data
@EqualsAndHashCode(exclude = {"categories"}) // 排除循環引用
@ToString(exclude = {"categories"}) // 排除循環引用
public class RecommendModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserModel user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private StoreModel store;

    @Column(nullable = false, length = 500)
    private String reason;

    @Column(nullable = false)
    private Integer score; // 1-5 星

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "recommend_category",
        joinColumns = @JoinColumn(name = "recommend_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<CategoryModel> categories = new HashSet<>();

    @Column(name = "photo_url", columnDefinition = "JSON")
    @Convert(converter = StringListToJsonConverter.class)
    private List<String> photoUrls;
}