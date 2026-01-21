package com.ntd.unsaid.domain.entity;

import com.ntd.unsaid.domain.enums.MediaType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Entity
@Table(name = "post_media")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    Post post;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    MediaType mediaType;

    @Column(nullable = false)
    String url;          // Cloudinary

    Integer width;       // optional
    Integer height;      // optional

    Long fileSize;       // bytes
    Integer duration;    // giây (chỉ cho video)

    Integer displayOrder;

    Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

}
