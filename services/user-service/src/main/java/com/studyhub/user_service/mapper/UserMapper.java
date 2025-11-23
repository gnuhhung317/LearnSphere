package com.studyhub.user_service.mapper;

import com.studyhub.user_service.dto.*;
import com.studyhub.user_service.entity.LearningPath;
import com.studyhub.user_service.entity.User;
import com.studyhub.user_service.entity.UserStats;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(CreateUserRequest dto);

    UserResponse toResponse(User user);

    List<UserResponse> toResponseList(List<User> users);
    

    // UserStats mappings

       UserStatsDto toStatsDto(UserStats userStats);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
       @Mapping(target = "updatedAt", ignore = true)

    void updateUserStatsFromDto(UpdateStatsRequest dto, @MappingTarget UserStats userStats);

    

       // LearningPath mappings
    LearningPathDto toLearningPathDto(LearningPath learningPath);

    List<LearningPathDto> toLearningPathDtoList(List<LearningPath> learningPaths);

    @Mapping(target = "id", ignore = true)
       @Mapping(target = "user", ignore = true)

    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    LearningPath toLearningPathEntity(CreateLearningPathRequest dto);

    // Profile view mapping
    @Mapping(source = "userId", target = "id")
    @Mapping(source = "userProfile.avatarUrl", target = "avatarUrl")
    @Mapping(source = "userProfile.bio", target = "bio")
    @Mapping(source = "userProfile.title", target = "title")
    @Mapping(source = "userProfile.socialLinks", target = "socialLinks")
    @Mapping(source = "userStats", target = "stats")
    @Mapping(source = "learningPaths", target = "learningPaths")
    UserProfileViewResponse toProfileViewResponse(User user);
}
