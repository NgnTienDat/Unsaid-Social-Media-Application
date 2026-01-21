package com.ntd.unsaid.presentation.controller;

import com.ntd.unsaid.application.dto.request.PostCreationRequest;
import com.ntd.unsaid.application.dto.response.PostResponse;
import com.ntd.unsaid.application.service.FeedService;
import com.ntd.unsaid.application.service.PostService;
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
@RequestMapping("/api/v1/home/feed")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HomeFeedController {

    FeedService feedService;

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getFeed(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtils.ok(
                        feedService.getFeed(jwt.getSubject(), page, size)));
    }

    @GetMapping("/slice")
    public ResponseEntity<ApiResponse<?>> getFeedSlice(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtils.ok(
                        feedService.getFeedForUser(jwt.getSubject(), page, size)));
    }



}
