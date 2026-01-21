package com.ntd.unsaid.domain.entity;

import com.ntd.unsaid.domain.enums.FollowStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Entity
@Table(
        name = "follows",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_follows_follower_following",
                        columnNames = {"follower_id", "following_id"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_follows_follower_id",
                        columnList = "follower_id"
                ),
                @Index(
                        name = "idx_follows_following_id",
                        columnList = "following_id"
                ),
                @Index(
                        name = "idx_follows_following_created_at",
                        columnList = "following_id, created_at"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Follow {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, length = 36)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    User follower;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id", nullable = false)
    User following;

    @Enumerated(EnumType.STRING)
    FollowStatus status;

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
