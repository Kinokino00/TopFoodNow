package com.example.topfoodnow.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "store")
@Data
@Schema(description = "店家資料")
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

    @Column(name = "photo_url", nullable = false, length = 255)
    @Schema(description = "店家照片URL", example = "http://example.com/store/photo.jpg")
    private String photoUrl;

    @Column(name = "created_at", nullable = false)
    @Schema(description = "創建時間", example = "2023-01-01T12:00:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
