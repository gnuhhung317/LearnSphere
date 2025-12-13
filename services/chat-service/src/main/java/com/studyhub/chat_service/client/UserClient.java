package com.studyhub.chat_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "${user-service.url:http://localhost:8081}")
public interface UserClient {

    @GetMapping("/api/v1/users/{userId}")
    UserInfo getUserById(@PathVariable("userId") Long userId);

    @GetMapping("/api/v1/users/keycloak/{keycloakId}")
    UserInfo getUserByKeycloakId(@PathVariable("keycloakId") String keycloakId);

    class UserInfo {

        private Long id;
        private String username;
        private String fullName;
        private String avatarUrl;

        public UserInfo() {
        }

        public UserInfo(Long id, String username, String fullName, String avatarUrl) {
            this.id = id;
            this.username = username;
            this.fullName = fullName;
            this.avatarUrl = avatarUrl;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
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
