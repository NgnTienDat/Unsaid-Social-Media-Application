package com.ntd.unsaid.application.mapper;

import com.ntd.unsaid.application.dto.FeedPostDTO;
import com.ntd.unsaid.application.dto.response.PostResponse;
import com.ntd.unsaid.domain.entity.Post;
import com.ntd.unsaid.domain.entity.PostMedia;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PostMapper {
    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "authorUsername", source = "author.username")
    PostResponse toResponse(Post post);

    @Mapping(target = "postId", source = "id")
    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "authorUsername", source = "author.username")
    @Mapping(target = "authorFullName", source = "author.fullName")
    @Mapping(target = "authorAvatar", source = "author.avatar")
    @Mapping(target = "mediaUrls", source = "media", qualifiedByName = "mapMediaToUrls")
    FeedPostDTO toFeedPostDTO(Post post);

    @Named("mapMediaToUrls")
    default List<String> mapMediaToUrls(List<PostMedia> media) {
        if (media == null) return null;
        return media.stream()
                .map(PostMedia::getUrl)
                .toList();
    }
}
