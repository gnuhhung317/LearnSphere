package com.studyhub.chat_service.client;

import com.studyhub.common.dto.KeycloakUserIdList;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "user-service",configuration = AuthRequestInterceptor.class)
public interface UserClient {

    @GetMapping("/api/v1/users/{userId}")
    UserInfo getUserById(@PathVariable("userId") String userId);

    @GetMapping("/api/v1/users/basic/{keycloakId}")
    UserInfo getBasicById(@PathVariable("keycloakId") String keycloakId);

    @GetMapping("/api/v1/users/basic/bulk")
    List<UserInfo> getBasicBulkByIds(@RequestBody KeycloakUserIdList keycloakIds);

    class UserInfo {

        private String id;
        private String username;
        private String fullName;
        private String avatarUrl;

        public UserInfo() {
        }

        public UserInfo(String id, String username, String fullName, String avatarUrl) {
            this.id = id;
            this.username = username;
            this.fullName = fullName;
            this.avatarUrl = avatarUrl;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }

        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }
    }
}
