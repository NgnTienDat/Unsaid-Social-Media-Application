package com.ntd.unsaid.domain.event;

import java.time.Instant;

public record PostCreatedEvent(
        String postId,
        String authorId,
        int followerCount,
        Instant createdAt
) {}