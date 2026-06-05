package com.tuan.ecommerce.modules.brand.domain;

import com.tuan.ecommerce.common.domain.BaseEntity;
import com.tuan.ecommerce.common.utils.SlugUtils;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "brands",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_brands_name", columnNames = "name"),
                @UniqueConstraint(name = "uk_brands_slug", columnNames = "slug")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Brand extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, unique = true)
    private String logoUrl;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    public void generateSlug() {
        if (this.slug == null || this.slug.isEmpty()) {
            this.slug = SlugUtils.makeSlug(this.name);
        }
    }
}
