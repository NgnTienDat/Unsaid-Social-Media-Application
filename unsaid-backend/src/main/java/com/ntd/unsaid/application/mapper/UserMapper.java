package com.ntd.unsaid.application.mapper;

import com.ntd.unsaid.application.dto.request.UserCreationRequest;
import com.ntd.unsaid.application.dto.response.FollowerResponse;
import com.ntd.unsaid.application.dto.response.UserResponse;
import com.ntd.unsaid.domain.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toUser(UserCreationRequest userCreationRequest);

//    @Mapping(source = "role", target = "role")
    UserResponse toResponse(User user);
    FollowerResponse toFollowerResponse(User user);
}
