package com.ntd.unsaid.controller;

import com.ntd.unsaid.dto.request.UserCreationRequest;
import com.ntd.unsaid.dto.response.UserResponse;
import com.ntd.unsaid.service.UserService;
import com.ntd.unsaid.utils.ApiResponse;
import com.ntd.unsaid.utils.ResponseUtils;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    UserService userService;


    @GetMapping("/health")
    public String healthCheck() {
        return "User Service is up and running!";
    }


    @PostMapping("/new-user")
    public ResponseEntity<ApiResponse<?>> createUser(@RequestBody @Valid UserCreationRequest userRequest) {
        userService.createUser(userRequest);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseUtils.created(null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ResponseUtils.ok(userService.getCurrentUser(jwt)));
    }
}
