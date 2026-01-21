package com.ntd.unsaid.application.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.ntd.unsaid.application.dto.request.MediaUploadResult;
import com.ntd.unsaid.domain.enums.ErrorCode;
import com.ntd.unsaid.domain.enums.MediaType;
import com.ntd.unsaid.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
@Service
@RequiredArgsConstructor
public class CloudinaryUploadService {

    private final Cloudinary cloudinary;

    public MediaUploadResult upload(MultipartFile file) {

        try {
            String contentType = file.getContentType();
            boolean isVideo = contentType != null && contentType.startsWith("video");

            Map<String, Object> options = ObjectUtils.asMap(
                    "resource_type", isVideo ? "video" : "image",
                    "folder", "posts"
            );

            Map<?, ?> result = cloudinary.uploader()
                    .upload(file.getBytes(), options);

            Number bytes = (Number) result.get("bytes");
            Number duration = (Number) result.get("duration");

            MediaUploadResult upload = MediaUploadResult.builder()
                    .url((String) result.get("secure_url"))
                    .width((Integer) result.get("width"))
                    .height((Integer) result.get("height"))
                    .fileSize(bytes != null ? bytes.longValue() : null)
                    .build();

            if (isVideo) {
                upload.setMediaType(MediaType.VIDEO);
                upload.setDuration(duration != null ? duration.intValue() : null);
            } else {
                upload.setMediaType(MediaType.IMAGE);
            }

            return upload;

        } catch (IOException e) {
            throw new AppException(ErrorCode.MEDIA_UPLOAD_FAILED);
        }
    }
}
