package com.ntd.unsaid.controller;

import com.ntd.unsaid.dto.request.PostCreationRequest;
import com.ntd.unsaid.dto.request.UserCreationRequest;
import com.ntd.unsaid.dto.request.UserUpdateRequest;
import com.ntd.unsaid.dto.response.PostResponse;
import com.ntd.unsaid.dto.response.UserResponse;
import com.ntd.unsaid.service.PostService;
import com.ntd.unsaid.service.UserService;
import com.ntd.unsaid.utils.ApiResponse;
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

import java.util.List;


@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostController {

    PostService postService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @AuthenticationPrincipal Jwt jwt,
            @RequestPart("data") @Valid PostCreationRequest request,
            @RequestPart(value = "media", required = false) List<MultipartFile> mediaFiles
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResponseUtils.created(postService.createPost(jwt.getSubject(), request, mediaFiles)));
    }


}
