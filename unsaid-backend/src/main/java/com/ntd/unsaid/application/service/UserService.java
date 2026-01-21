package com.ntd.unsaid.application.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.ntd.unsaid.application.dto.request.UserCreationRequest;
import com.ntd.unsaid.application.dto.request.UserUpdateRequest;
import com.ntd.unsaid.application.dto.response.FollowerResponse;
import com.ntd.unsaid.application.dto.response.UserResponse;
import com.ntd.unsaid.domain.entity.Follow;
import com.ntd.unsaid.domain.entity.User;
import com.ntd.unsaid.domain.enums.ErrorCode;
import com.ntd.unsaid.domain.enums.FollowStatus;
import com.ntd.unsaid.domain.enums.UserRole;
import com.ntd.unsaid.domain.enums.UserStatus;
import com.ntd.unsaid.exception.AppException;
import com.ntd.unsaid.application.mapper.UserMapper;
import com.ntd.unsaid.domain.repository.FollowRepository;
import com.ntd.unsaid.domain.repository.UserRepository;
import com.ntd.unsaid.utils.PageResponse;
import com.ntd.unsaid.utils.PageResponseUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;
    FollowRepository followRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    Cloudinary cloudinary;

    @Transactional
    public UserResponse createUser(UserCreationRequest userCreation) {
        if (this.userRepository.existsByEmail(userCreation.getEmail())) {
            throw new AppException(ErrorCode.USER_ALREADY_EXISTED);
        }
        User user = this.userMapper.toUser(userCreation);
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);
        user.setActive(true);
        user.setPassword(this.passwordEncoder.encode(userCreation.getPassword()));

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            throw new AppException(ErrorCode.USER_ALREADY_EXISTED);
        }
        return userMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(Jwt jwt) {
        String userEmail = jwt.getClaimAsString("sub");
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return userMapper.toResponse(user);
    }


    @Transactional
    public UserResponse updateMyProfile(Jwt jwt, UserUpdateRequest request, MultipartFile avatar) {
        String userEmail = jwt.getSubject();

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        boolean isUpdated = false;
        if (request.getFullName() != null &&
                !request.getFullName().equals(user.getFullName())) {
            user.setFullName(request.getFullName());
            isUpdated = true;
        }

        if (request.getUsername() != null &&
                !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new AppException(ErrorCode.USERNAME_ALREADY_EXISTED);
            }
            user.setUsername(request.getUsername());
            isUpdated = true;
        }

        if (avatar != null && !avatar.isEmpty()) {
            try {
                Map uploadResult = cloudinary.uploader().upload(avatar.getBytes(),
                        ObjectUtils.asMap("resource_type", "auto"));

                String avatarUrl = (String) uploadResult.get("secure_url");

                user.setAvatar(avatarUrl);
                isUpdated = true;

            } catch (IOException e) {
                throw new AppException(ErrorCode.UPLOAD_FILE_FAILED);
            }
        }

        if (!isUpdated) {
            return userMapper.toResponse(user);
        }
        userRepository.save(user);
        return userMapper.toResponse(user);
    }

    @Transactional
    public void followUser(String currentUserEmail, String targetUserId) {

        User follower = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String currentUserId = follower.getId();

        if (currentUserId.equals(targetUserId)) {
            throw new AppException(ErrorCode.CANNOT_FOLLOW_YOURSELF);
        }


        User following = userRepository.findById(targetUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Optional<Follow> existingFollow =
                followRepository.findByFollowerIdAndFollowingId(currentUserId, targetUserId);

        if (existingFollow.isPresent()) {
            Follow follow = existingFollow.get();

            if (follow.getStatus() == FollowStatus.ACCEPTED) {
                throw new AppException(ErrorCode.ALREADY_FOLLOWING);
            } else throw new RuntimeException(
                    "Follow request is already sended, current status: " + follow.getStatus()
            );
        }

        /// In case target user has private account, set status to PENDING
        Follow follow = Follow.builder()
                .follower(follower)
                .following(following)
                .status(following.getStatus().equals(UserStatus.PRIVATE) ?
                        FollowStatus.PENDING : FollowStatus.ACCEPTED)
                .build();

        followRepository.save(follow);
    }


    @Transactional
    public void unfollowUser(String currentUserEmail, String targetUserId) {

        User follower = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String currentUserId = follower.getId();

        if (currentUserId.equals(targetUserId)) {
            throw new AppException(ErrorCode.CANNOT_UNFOLLOW_YOURSELF);
        }

        Follow follow = followRepository
                .findByFollowerIdAndFollowingId(currentUserId, targetUserId)
                .orElseThrow(() -> new AppException(ErrorCode.FOLLOW_NOT_FOUND));
        followRepository.delete(follow);
    }

    @Transactional(readOnly = true)
    public PageResponse<FollowerResponse> getFollowers(String targetUserId, int page, int size, String type) {

        Pageable pageable = PageRequest.of(page, size);
        Page<FollowerResponse> pageFollows;
        if (type.equalsIgnoreCase("followers")) {
            pageFollows = followRepository.findFollowersDto(targetUserId, pageable);
        } else if (type.equalsIgnoreCase("following")) {
            pageFollows = followRepository.findFollowingDto(targetUserId, pageable);
        } else {
            throw new AppException(ErrorCode.INVALID_FOLLOW_TYPE);
        }


        return PageResponseUtils.build(pageFollows, Function.identity());
    }


}
