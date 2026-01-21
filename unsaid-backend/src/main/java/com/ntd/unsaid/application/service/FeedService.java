package com.ntd.unsaid.application.service;

import com.ntd.unsaid.application.dto.request.MediaUploadResult;
import com.ntd.unsaid.application.dto.request.PostCreationRequest;
import com.ntd.unsaid.application.dto.response.PostResponse;
import com.ntd.unsaid.application.mapper.PostMapper;
import com.ntd.unsaid.domain.entity.Post;
import com.ntd.unsaid.domain.entity.PostMedia;
import com.ntd.unsaid.domain.entity.User;
import com.ntd.unsaid.domain.enums.ErrorCode;
import com.ntd.unsaid.domain.enums.MediaType;
import com.ntd.unsaid.domain.repository.PostRepository;
import com.ntd.unsaid.domain.repository.UserRepository;
import com.ntd.unsaid.exception.AppException;
import com.ntd.unsaid.utils.PageResponse;
import com.ntd.unsaid.utils.PageResponseUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FeedService {
    PostRepository postRepository;
    PostMapper postMapper;

    @Transactional(readOnly = true)
    public PageResponse<PostResponse> getFeed(
            String userId,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Post> posts = postRepository.findFeedPosts(
                userId,
                pageable
        );

        return PageResponseUtils.build(posts, postMapper::toResponse);
    }

    public Slice<Post> getFeedForUser(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        // scanLimit: Số lượng bài tối đa lấy từ mỗi nguồn (Friend/Public) để so sánh.
        // Nếu page size là 20, scanLimit = 100 là dư giả cho việc trộn lẫn.
        // Logic: "Lấy top 100 bài mới nhất của bạn bè + top 100 bài Public, gộp lại rồi cắt ra 20 bài cho user"
        int scanLimit = 100;

        return postRepository.findFeedPosts(userId, scanLimit, pageable);
    }

}
