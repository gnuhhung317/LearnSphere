package com.studyhub.user_service.entity;

import com.studyhub.common.constant.enums.SocialPlatform;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Entity
@Table(name = "user_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "avatar_url")
    private String avatarUrl;
    @Column(name = "bio")
    private String bio;
    @Column(name = "title")
    private String title;
    @Column(name = "social_links",columnDefinition = "jsonb")
    @JdbcTypeCode(value = SqlTypes.JSON)
    private Map<SocialPlatform, String> socialLinks;
}
