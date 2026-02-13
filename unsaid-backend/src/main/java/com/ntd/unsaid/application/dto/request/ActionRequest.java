package com.ntd.unsaid.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ActionRequest {

    @NotBlank(message = "Post ID is required")
    String postId;

    @NotBlank(message = "Action type is required")
    String actionType;
}
