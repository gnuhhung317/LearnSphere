package com.studyhub.user_service.mapper;

import com.studyhub.user_service.dto.CreateUserRequest;
import com.studyhub.user_service.dto.UserResponse;
import com.studyhub.user_service.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {


    User toEntity(CreateUserRequest dto);

    UserResponse toResponse(User user);

    List<UserResponse> toResponseList(List<User> users);

//    default String getFirstName(String fullName) {
//        if (fullName == null) {
//            return null;
//        }
//        String trimmed = fullName.trim();
//        if (trimmed.isEmpty()) return null;
//        String[] parts = trimmed.split("\\s+", 2);
//        return parts.length > 0 ? parts[0] : null;
//    }
//
//    default String getLastName(String fullName) {
//        if (fullName == null) {
//            return null;
//        }
//        String trimmed = fullName.trim();
//        if (trimmed.isEmpty()) return null;
//        String[] parts = trimmed.split("\\s+", 2);
//        return parts.length > 1 ? parts[1] : null;
//    }
}
