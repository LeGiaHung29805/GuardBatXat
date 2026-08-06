package com.example.GuardBatXat.mapper;
import com.example.GuardBatXat.entity.User;

import com.example.GuardBatXat.dto.request.auth.UserProfileRequest;
import com.example.GuardBatXat.dto.response.auth.UserProfileResponse;
import com.example.GuardBatXat.entity.UserProfile;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {

    @Mapping(source = "user.defaultBuilding.id", target = "defaultBuildingId")
    @Mapping(source = "user.trustScore", target = "trustScore")
    UserProfileResponse toResponse(UserProfile profile);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(UserProfileRequest request, @MappingTarget UserProfile profile);
}