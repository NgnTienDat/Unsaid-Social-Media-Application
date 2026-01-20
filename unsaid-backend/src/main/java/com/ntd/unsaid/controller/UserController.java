package com.ntd.unsaid.controller;

import com.ntd.unsaid.dto.request.UserCreationRequest;
import com.ntd.unsaid.dto.request.UserUpdateRequest;
import com.ntd.unsaid.dto.response.FollowerResponse;
import com.ntd.unsaid.dto.response.UserResponse;
import com.ntd.unsaid.service.UserService;
import com.ntd.unsaid.utils.ApiResponse;
import com.ntd.unsaid.utils.PageResponse;
import com.ntd.unsaid.utils.ResponseUtils;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    UserService userService;


    @GetMapping("/health")
    public String healthCheck() {
        return "Unsaid is up and running!";
    }


    @PostMapping
    public ResponseEntity<ApiResponse<?>> createUser(@RequestBody @Valid UserCreationRequest userRequest) {
        userService.createUser(userRequest);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseUtils.created(null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ResponseUtils.ok(userService.getCurrentUser(jwt)));
    }

    @PutMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserResponse>> updateMyProfile(
            @AuthenticationPrincipal Jwt jwt,
            @RequestPart("data") @Valid UserUpdateRequest request,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar
    ) {
        return ResponseEntity.ok(ResponseUtils.ok(userService.updateMyProfile(jwt, request, avatar)));
    }



    @GetMapping("/{id}/follows")
    public ResponseEntity<ApiResponse<?>> getFollowers(
            @PathVariable("id") String targetUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "followers") String type
    ) {
        return ResponseEntity.ok(
                ResponseUtils.ok(userService.getFollowers(targetUserId, page, size, type))
        );
    }


    @PostMapping("/{id}/follows")
    public ResponseEntity<ApiResponse<?>> followUser(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") String targetUserId
    ) {
        userService.followUser(jwt.getSubject(), targetUserId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseUtils.created(null));
    }

    @DeleteMapping("/{id}/follows")
    public ResponseEntity<ApiResponse<?>> unfollowUser(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") String targetUserId
    ) {
        userService.unfollowUser(jwt.getSubject(), targetUserId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseUtils.ok(null));
    }


}
