package com.ntd.unsaid.dto.request;

import com.ntd.unsaid.entity.Post;
import com.ntd.unsaid.entity.PostMedia;
import com.ntd.unsaid.enums.AnonymityLevel;
import com.ntd.unsaid.enums.PostStatus;
import com.ntd.unsaid.enums.PostVisibility;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

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
