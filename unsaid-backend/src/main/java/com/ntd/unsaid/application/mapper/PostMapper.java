package com.ntd.unsaid.application.mapper;

import com.ntd.unsaid.application.dto.response.PostResponse;
import com.ntd.unsaid.domain.entity.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PostMapper {
    @Mapping(target = "authorId", source = "post.author.id")
    @Mapping(target = "authorUsername", source = "post.author.username")
    PostResponse toResponse(Post post);
}
