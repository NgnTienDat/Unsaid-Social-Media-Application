package com.ntd.unsaid.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ntd.unsaid.domain.enums.AnonymityLevel;
import com.ntd.unsaid.domain.enums.PostStatus;

import com.ntd.unsaid.domain.enums.PostVisibility;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "posts",
        indexes = {
                @Index(
                        name = "idx_posts_author_created",
                        columnList = "author_id, created_at DESC"
                ),
                @Index(
                        name = "idx_posts_status_author_created",
                        columnList = "status, author_id, created_at DESC"
                ),
                @Index(
                        name = "idx_posts_status_visibility_created",
                        columnList = "status, post_visibility, created_at DESC"
                ),
                @Index(
                        name = "idx_posts_parent",
                        columnList = "parent_post_id"
                ),
                @Index(
                        name = "idx_posts_repost",
                        columnList = "repost_of_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, length = 36)
    String id;

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    User author;

    @Column(length = 350)
    @Size(max = 350, message = "Post content must be at most 350 characters")
    String content;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    List<PostMedia> media = new ArrayList<>();

    @OneToMany(mappedBy = "parentPost", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    List<Post> childPosts = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_post_id")
    Post parentPost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "repost_of_id",
            foreignKey = @ForeignKey(
                    foreignKeyDefinition =
                            "FOREIGN KEY (repost_of_id) REFERENCES posts(id) ON DELETE SET NULL"
            )
    )
    Post repostOf;

    @Enumerated(EnumType.STRING)
    PostStatus status;

    @Enumerated(EnumType.STRING)
    PostVisibility postVisibility;

    @Enumerated(EnumType.STRING)
    AnonymityLevel anonymityLevel;

    @Min(value = 0, message = "Reply count must be non-negative")
    Integer replyCount;
    @Min(value = 0, message = "Number of like must be non-negative")
    Integer likeCount;
    String mention;

    Instant createdAt;
    Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
