package com.ntd.unsaid.application.service;

import com.ntd.unsaid.application.dto.response.PostResponse;
import com.ntd.unsaid.application.mapper.PostMapper;
import com.ntd.unsaid.domain.entity.Post;
import com.ntd.unsaid.domain.repository.FollowRepository;
import com.ntd.unsaid.domain.repository.PostRepository;
import com.ntd.unsaid.infrastructure.caching.FeedRedisRepository;
import com.ntd.unsaid.utils.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FeedService {

    PostRepository postRepository;
    FollowRepository followRepository;
    FeedRedisRepository feedRedisRepository;
    PostMapper postMapper;

    public void fanOutToFollowers(String postId, String authorId, long createdAt) {
        List<String> targetUserIds = new ArrayList<>();

        targetUserIds.add(authorId);

        List<String> followerIds = followRepository.findFollowerIdsByFollowingId(authorId);
        if (followerIds != null && !followerIds.isEmpty()) {
            if (followerIds.size() < Constant.CELEBRITY_FOLLOWER_THRESHOLD) {
                targetUserIds.addAll(followerIds);
            }
        }
        feedRedisRepository.pushPostToFollowers(postId, targetUserIds, createdAt);
    }

    @Transactional(readOnly = true)
    public PageResponse<PostResponse> getPageFeed(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Post> posts = postRepository.findFeedPosts(
                userId,
                pageable
        );

        return PageResponseUtils.build(posts, postMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public SliceResponse<PostResponse> getSliceFeed(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        // scanLimit: Số lượng bài tối đa lấy từ mỗi nguồn (Friend/Public) để so sánh.
        // Nếu page size là 20, scanLimit = 100 là dư giả cho việc trộn lẫn.
        // Logic: "Lấy top 100 bài mới nhất của bạn bè + top 100 bài Public, gộp lại rồi cắt ra 20 bài cho user"
        int scanLimit = 100;
        Slice<Post> slice = postRepository.findFeedPosts(userId, scanLimit, pageable);
        return SliceResponseUtils.build(slice, postMapper::toResponse);
    }



}
