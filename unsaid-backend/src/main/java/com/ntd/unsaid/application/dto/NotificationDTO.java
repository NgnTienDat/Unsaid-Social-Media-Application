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
public class NotificationDTO {
    String type;        // POST_CREATED
    String postId;
    String fromUserId;
    String message;
    long createdAt;
}

