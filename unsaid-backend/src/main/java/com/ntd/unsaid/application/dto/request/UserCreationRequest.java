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
public class UserCreationRequest {
    @Email(message = "INVALID_EMAIL")
    String email;

    @NotBlank(message = "full name is required")
    @Size(min = 1, max = 50, message = "Password must be at most 50 characters")
    String fullName;

    @NotBlank(message = "Username is required")
    @Size(min = 2, max = 100, message = "username must be between 2 and 100 characters")
    String username;

    @NotBlank(message = "NOT_BLANK")
    @Size(min = 6, message = "INVALID_PASSWORD")
    String password;
}
