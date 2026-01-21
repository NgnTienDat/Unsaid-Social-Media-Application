package com.ntd.unsaid.application.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FollowerResponse {
    String id;
    String username;
    String fullName;
    String avatar;
}
