package com.ntd.unsaid.domain.event;

import java.time.Instant;

public record PostCreatedEvent(
        String postId,
        String authorId,
        Instant createdAt
) {}