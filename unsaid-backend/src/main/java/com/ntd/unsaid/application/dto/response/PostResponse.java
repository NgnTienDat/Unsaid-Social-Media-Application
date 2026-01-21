package com.ntd.unsaid.application.dto.response;

import com.ntd.unsaid.domain.enums.AnonymityLevel;
import com.ntd.unsaid.domain.enums.PostVisibility;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostResponse {
    String id;
    String content;

    String authorId;
    String authorUsername;

    List<PostMediaResponse> media;

    String parentPostId;
    String repostOfId;

    Integer replyCount;
    Integer likeCount;

    PostVisibility postVisibility;
    AnonymityLevel anonymityLevel;

    Instant createdAt;
}
