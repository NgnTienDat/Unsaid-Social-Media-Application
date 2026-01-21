package com.ntd.unsaid.application.dto.request;

import com.ntd.unsaid.domain.enums.MediaType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MediaUploadResult {

    MediaType mediaType;
    String url;

    Integer width;
    Integer height;

    Long fileSize;
    Integer duration; // seconds (video)
}
