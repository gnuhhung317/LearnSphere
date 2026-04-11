package com.studyhub.realtime_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "livekit")
@Data
public class LiveKitConfig {
    private String url;
    private String apiKey;
    private String apiSecret;
}
