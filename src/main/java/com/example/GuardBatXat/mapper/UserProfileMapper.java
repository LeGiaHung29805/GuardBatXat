package com.example.GuardBatXat.mapper;
import com.example.GuardBatXat.entity.User;

import com.example.GuardBatXat.dto.request.auth.UserProfileRequest;
import com.example.GuardBatXat.dto.response.auth.UserProfileResponse;
import com.example.GuardBatXat.entity.UserProfile;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {

    UserProfileResponse toResponse(UserProfile profile);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(UserProfileRequest request, @MappingTarget UserProfile profile);
}