package com.ntd.unsaid.application.dto;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FeedPostDTO implements Serializable {

    String postId;

    // Author snapshot (denormalized)
    String authorId;
    String authorUsername;
    String authorFullName;
    String authorAvatar;

    // Content
    String content;
    List<String> mediaUrls;

    // Counters (eventually consistent)
    long likeCount;
    long replyCount;

    // Feed ordering
    Instant createdAt;
}