package com.ntd.unsaid.application.dto.response;

import com.ntd.unsaid.domain.enums.MediaType;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostMediaResponse {

    String id;
    MediaType mediaType;
    String url;

    Integer width;
    Integer height;
    Integer duration;

    Integer displayOrder;
}
