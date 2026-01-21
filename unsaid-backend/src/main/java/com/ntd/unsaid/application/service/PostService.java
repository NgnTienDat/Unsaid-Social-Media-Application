package com.ntd.unsaid.application.service;

import com.ntd.unsaid.domain.enums.ErrorCode;
import com.ntd.unsaid.domain.enums.MediaType;
import com.ntd.unsaid.application.dto.request.MediaUploadResult;
import com.ntd.unsaid.application.dto.request.PostCreationRequest;
import com.ntd.unsaid.application.dto.response.PostResponse;
import com.ntd.unsaid.domain.entity.Post;
import com.ntd.unsaid.domain.entity.PostMedia;
import com.ntd.unsaid.domain.entity.User;
import com.ntd.unsaid.exception.AppException;
import com.ntd.unsaid.application.mapper.PostMapper;
import com.ntd.unsaid.domain.repository.PostRepository;
import com.ntd.unsaid.domain.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostService {
    PostRepository postRepository;
    UserRepository userRepository;
    CloudinaryUploadService cloudinaryUploadService;
    PostMapper postMapper;

    @Transactional
    public PostResponse createPost(
            String userEmail,
            PostCreationRequest request,
            List<MultipartFile> mediaFiles
    ) {

        User author = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        boolean hasContent = request.getContent() != null && !request.getContent().trim().isEmpty();
        boolean hasMedia = mediaFiles != null && !mediaFiles.isEmpty();

        if (!hasContent && !hasMedia) throw new AppException(ErrorCode.POST_CONTENT_OR_MEDIA_REQUIRED);
        if (mediaFiles != null && mediaFiles.size() > 10) throw new AppException(ErrorCode.MEDIA_LIMIT_EXCEEDED);


        Post post = Post.builder()
                .author(author)
                .content(request.getContent())
                .postVisibility(request.getPostVisibility())
                .anonymityLevel(request.getAnonymityLevel())
                .status(request.getStatus())
                .replyCount(0)
                .likeCount(0)
                .build();

        // Parent post (reply)
        if (request.getParentPostId() != null) {
            Post parent = postRepository.findById(request.getParentPostId())
                    .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
            post.setParentPost(parent);
            parent.setReplyCount(parent.getReplyCount() + 1);
        }

        // Repost
        if (request.getRepostOfId() != null) {
            Post repostOf = postRepository.findById(request.getRepostOfId())
                    .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
            post.setRepostOf(repostOf);
        }

        postRepository.save(post);

        if (mediaFiles != null && !mediaFiles.isEmpty()) {

            if (mediaFiles.size() > 10) throw new AppException(ErrorCode.MEDIA_LIMIT_EXCEEDED);
            int order = 0;

            for (MultipartFile file : mediaFiles) {
                MediaUploadResult upload = cloudinaryUploadService.upload(file);
                // validate video duration <= 5 minutes
                if (upload.getMediaType() == MediaType.VIDEO &&
                        upload.getDuration() != null &&
                        upload.getDuration() > 300) {
                    throw new AppException(ErrorCode.VIDEO_DURATION_EXCEEDED);
                }

                PostMedia media = PostMedia.builder()
                        .post(post)
                        .mediaType(upload.getMediaType())
                        .url(upload.getUrl())
                        .width(upload.getWidth())
                        .height(upload.getHeight())
                        .duration(upload.getDuration())
                        .fileSize(upload.getFileSize())
                        .displayOrder(order++)
                        .build();

                post.getMedia().add(media);
            }
        }
        return postMapper.toResponse(post);
    }
}
