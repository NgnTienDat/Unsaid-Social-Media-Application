package com.ntd.unsaid.presentation.controller;

import com.ntd.unsaid.application.dto.request.UserCreationRequest;
import com.ntd.unsaid.application.dto.request.UserUpdateRequest;
import com.ntd.unsaid.application.dto.response.UserResponse;
import com.ntd.unsaid.application.service.UserService;
import com.ntd.unsaid.infrastructure.caching.RedisDataSeeder;
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


@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminController {

    UserService userService;
    RedisDataSeeder redisDataSeeder;

    @PostMapping("/redis-data-seeder")
    public ResponseEntity<ApiResponse<?>> followUser() {
        redisDataSeeder.seedRedisForStressTest();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseUtils.created(null));
    }

}
