package com.ntd.unsaid.application.dto.request;

import com.ntd.unsaid.domain.enums.AnonymityLevel;
import com.ntd.unsaid.domain.enums.PostStatus;
import com.ntd.unsaid.domain.enums.PostVisibility;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostCreationRequest {

    @Size(max = 350, message = "Post content must be at most 350 characters")
    String content;

    String parentPostId; // reply to post
    String repostOfId;  // repost of post

    PostStatus status; // active, deleted, archived
    PostVisibility postVisibility; // public, followers-only, private
    AnonymityLevel anonymityLevel; // anonymous, identified
}
