package com.ntd.unsaid.domain.event;

import lombok.Builder;

import java.time.Instant;

@Builder
public record PostCreatedMessage(
        String postId,
        String authorId,
        int followerCount,
        long createdAt
) {}