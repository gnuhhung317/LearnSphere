package com.studyhub.user_service.dto;

import com.studyhub.common.constant.enums.SocialPlatform;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for updating user profile information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserProfileRequest {

    
    @Size(max = 50, message = "Username must not exceed 50 characters")
       private String username;

       @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;
    
    @Size(max = 100, message = "Location must not exceed 100 characters")
       private String location;

       @Size(max = 500, message = "Bio must not exceed 500 characters")
       private String bio;

    @Size(max = 100, message = "Title must not exceed 100 characters")
    private String title;

    private Map<SocialPlatform, String> socialLinks;

    @Size(max = 500, message = "Avatar URL must not exceed 500 characters")
    private String avatarUrl;
}
