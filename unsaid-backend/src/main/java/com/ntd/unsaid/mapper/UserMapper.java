package com.ntd.unsaid.mapper;

import com.ntd.unsaid.dto.request.UserCreationRequest;
import com.ntd.unsaid.dto.response.FollowerResponse;
import com.ntd.unsaid.dto.response.UserResponse;
import com.ntd.unsaid.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toUser(UserCreationRequest userCreationRequest);

//    @Mapping(source = "role", target = "role")
    UserResponse toResponse(User user);
    FollowerResponse toFollowerResponse(User user);
}
