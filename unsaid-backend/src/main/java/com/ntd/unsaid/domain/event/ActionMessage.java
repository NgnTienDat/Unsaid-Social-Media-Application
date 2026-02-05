package com.ntd.unsaid.domain.event;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Builder
public record ActionMessage(
        String userId,
        String postId,
        String actionType,
        Instant createdAt
) {}