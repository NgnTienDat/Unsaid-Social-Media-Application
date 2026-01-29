package com.ntd.unsaid.presentation.controller;

import com.ntd.unsaid.application.service.FeedService;
import com.ntd.unsaid.utils.ApiResponse;
import com.ntd.unsaid.utils.ResponseUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/home/feed")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SocialFeedController {

    FeedService feedService;

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getPageFeed(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtils.ok(
                        feedService.getPageFeed(jwt.getSubject(), page, size)));
    }

//    @GetMapping("/slice")
//    public ResponseEntity<ApiResponse<?>> getSliceFeed(
////            @AuthenticationPrincipal Jwt jwt,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int size,
//            @RequestParam("id") String userId
//    ) {
//
//        return ResponseEntity.ok(
//                ResponseUtils.ok(feedService.getSliceFeed(userId, page, size)));
//    }


    @GetMapping("/slice")
    public ResponseEntity<ApiResponse<?>> getSliceFeed(
//            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam("id") String userId
    ) {

        return ResponseEntity.ok(
                ResponseUtils.ok(feedService.getFeed(userId, page)));
    }


}
