package com.example.GuardBatXat.mapper;

import com.example.GuardBatXat.dto.request.auth.UserCreationRequest;
import com.example.GuardBatXat.dto.response.auth.UserResponse;
import com.example.GuardBatXat.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "role", ignore = true) // Sẽ gán thủ công ở Service để đảm bảo an toàn
    User toUser(UserCreationRequest request);

    @Mapping(source = "role.roleName", target = "roleName")
    UserResponse toUserResponse(User user);

}