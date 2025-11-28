package com.studyhub.api_gateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import static org.springframework.cloud.gateway.support.RouteMetadataUtils.CONNECT_TIMEOUT_ATTR;
import static org.springframework.cloud.gateway.support.RouteMetadataUtils.RESPONSE_TIMEOUT_ATTR;

/**
 * Java-based Route Configuration for Spring Cloud Gateway Alternative to
 * YAML-based configuration to avoid deprecation warnings Based on Context7 best
 * practices for Spring Cloud Gateway
 */
@Configuration
public class RoutesConfig {

    private final KeyResolver ipKeyResolver;

    public RoutesConfig(@Autowired KeyResolver ipKeyResolver) {
        this.ipKeyResolver = ipKeyResolver;
    }

    /**
     * Redis Rate Limiter Bean for User Service This is marked as @Primary to
     * serve as the default RateLimiter for Spring Cloud Gateway
     */
    @Bean
    @Primary
    public RedisRateLimiter userServiceRateLimiter() {
        return new RedisRateLimiter(10, 20, 1);
    }

    /**
     * Redis Rate Limiter Bean for Auth Service
     */
    @Bean
    public RedisRateLimiter authServiceRateLimiter() {
        return new RedisRateLimiter(20, 40, 1);
    }

    /**
     * Redis Rate Limiter Bean for Chat Service
     */
    @Bean
    public RedisRateLimiter chatServiceRateLimiter() {
        return new RedisRateLimiter(15, 30, 1);
    }

    /**
     * Redis Rate Limiter Bean for Media Service
     */
    @Bean
    public RedisRateLimiter mediaServiceRateLimiter() {
        return new RedisRateLimiter(5, 10, 1);
    }

    /**
     * Redis Rate Limiter Bean for AI Service
     */
    @Bean
    public RedisRateLimiter aiServiceRateLimiter() {
        return new RedisRateLimiter(2, 5, 1);
    }

    /**
     * Redis Rate Limiter Bean for Search Service
     */
    @Bean
    public RedisRateLimiter searchServiceRateLimiter() {
        return new RedisRateLimiter(8, 15, 1);
    }

    /**
     * Development routes configuration using Java DSL This configuration
     * provides a non-deprecated alternative to YAML routes
     */
    @Bean
    @Profile("dev")
    public RouteLocator devRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                // User Service Routes
                .route("user-service", r -> r.path("/api/users/**")
                .filters(f -> f
                .circuitBreaker(c -> c.setName("user-service").setFallbackUri("forward:/fallback/users"))
                .requestRateLimiter(c -> c.setRateLimiter(userServiceRateLimiter()).setKeyResolver(ipKeyResolver)))
                .metadata(RESPONSE_TIMEOUT_ATTR, 5000)
                .metadata(CONNECT_TIMEOUT_ATTR, 2000)
                .uri("http://localhost:8081"))
                // Auth Service Routes
                .route("auth-service", r -> r.path("/api/auth/**")
                .filters(f -> f
                .circuitBreaker(c -> c.setName("auth-service").setFallbackUri("forward:/fallback/auth"))
                .requestRateLimiter(c -> c.setRateLimiter(authServiceRateLimiter()).setKeyResolver(ipKeyResolver)))
                .metadata(RESPONSE_TIMEOUT_ATTR, 3000)
                .metadata(CONNECT_TIMEOUT_ATTR, 2000)
                .uri("http://localhost:8082"))
                // Chat Service Routes (REST API)
                .route("chat-service-api", r -> r.path("/api/chat/v1/**")
                .filters(f -> f
                .rewritePath("/api/chat/v1/(?<segment>.*)", "/api/v1/${segment}")
                .circuitBreaker(c -> c.setName("chat-service").setFallbackUri("forward:/fallback/chat"))
                .requestRateLimiter(c -> c.setRateLimiter(chatServiceRateLimiter()).setKeyResolver(ipKeyResolver)))
                .metadata(RESPONSE_TIMEOUT_ATTR, 10000)
                .metadata(CONNECT_TIMEOUT_ATTR, 3000)
                .uri("http://localhost:8083"))
                // Chat Service WebSocket Route
                .route("chat-service-ws", r -> r.path("/api/chat/ws/**")
                .filters(f -> f.rewritePath("/api/chat/ws/(?<segment>.*)", "/ws/${segment}"))
                .metadata(RESPONSE_TIMEOUT_ATTR, 30000)
                .metadata(CONNECT_TIMEOUT_ATTR, 5000)
                .uri("http://localhost:8083"))
                // Media Service Routes
                .route("media-service", r -> r.path("/api/media/**")
                .filters(f -> f
                .circuitBreaker(c -> c.setName("media-service").setFallbackUri("forward:/fallback/media"))
                .requestRateLimiter(c -> c.setRateLimiter(mediaServiceRateLimiter()).setKeyResolver(ipKeyResolver)))
                .metadata(RESPONSE_TIMEOUT_ATTR, 30000)
                .metadata(CONNECT_TIMEOUT_ATTR, 5000)
                .uri("http://localhost:8084"))
                // Real-time Service Routes
                .route("realtime-service", r -> r.path("/api/realtime/**")
                .filters(f -> f
                .circuitBreaker(c -> c.setName("realtime-service").setFallbackUri("forward:/fallback/realtime")))
                .metadata(RESPONSE_TIMEOUT_ATTR, 15000)
                .metadata(CONNECT_TIMEOUT_ATTR, 3000)
                .uri("http://localhost:8085"))
                // AI Service Routes
                .route("ai-service", r -> r.path("/api/ai/**")
                .filters(f -> f
                .circuitBreaker(c -> c.setName("ai-service").setFallbackUri("forward:/fallback/ai"))
                .requestRateLimiter(c -> c.setRateLimiter(aiServiceRateLimiter()).setKeyResolver(ipKeyResolver)))
                .metadata(RESPONSE_TIMEOUT_ATTR, 60000)
                .metadata(CONNECT_TIMEOUT_ATTR, 5000)
                .uri("http://localhost:8086"))
                // Search Service Routes
                .route("search-service", r -> r.path("/api/search/**")
                .filters(f -> f
                .circuitBreaker(c -> c.setName("search-service").setFallbackUri("forward:/fallback/search"))
                .requestRateLimiter(c -> c.setRateLimiter(searchServiceRateLimiter()).setKeyResolver(ipKeyResolver)))
                .metadata(RESPONSE_TIMEOUT_ATTR, 8000)
                .metadata(CONNECT_TIMEOUT_ATTR, 3000)
                .uri("http://localhost:8087"))
                // Keycloak Routes
                .route("keycloak", r -> r.path("/realms/**", "/admin/**")
                .metadata(RESPONSE_TIMEOUT_ATTR, 5000)
                .metadata(CONNECT_TIMEOUT_ATTR, 2000)
                .uri("http://localhost:8080"))
                .build();
    }

    /**
     * Production routes configuration Uses container service names instead of
     * localhost
     */
    @Bean
    @Profile("prod")
    public RouteLocator prodRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                // User Service Routes
                .route("user-service", r -> r.path("/api/users/**")
                .filters(f -> f
                .circuitBreaker(c -> c.setName("user-service").setFallbackUri("forward:/fallback/users"))
                .requestRateLimiter(c -> c.setRateLimiter(userServiceRateLimiter()).setKeyResolver(ipKeyResolver)))
                .uri("http://user-service:8081"))
                // Auth Service Routes
                .route("auth-service", r -> r.path("/api/auth/**")
                .filters(f -> f
                .circuitBreaker(c -> c.setName("auth-service").setFallbackUri("forward:/fallback/auth"))
                .requestRateLimiter(c -> c.setRateLimiter(authServiceRateLimiter()).setKeyResolver(ipKeyResolver)))
                .uri("http://auth-service:8082"))
                // Chat Service Routes (REST API)
                .route("chat-service-api", r -> r.path("/api/chat/v1/**")
                .filters(f -> f
                .rewritePath("/api/chat/v1/(?<segment>.*)", "/api/v1/${segment}")
                .circuitBreaker(c -> c.setName("chat-service").setFallbackUri("forward:/fallback/chat"))
                .requestRateLimiter(c -> c.setRateLimiter(chatServiceRateLimiter()).setKeyResolver(ipKeyResolver)))
                .uri("http://chat-service:8083"))
                // Chat Service WebSocket Route
                .route("chat-service-ws", r -> r.path("/api/chat/ws/**")
                .filters(f -> f.rewritePath("/api/chat/ws/(?<segment>.*)", "/ws/${segment}"))
                .uri("http://chat-service:8083"))
                // Media Service Routes  
                .route("media-service", r -> r.path("/api/media/**")
                .filters(f -> f
                .circuitBreaker(c -> c.setName("media-service").setFallbackUri("forward:/fallback/media"))
                .requestRateLimiter(c -> c.setRateLimiter(searchServiceRateLimiter()).setKeyResolver(ipKeyResolver)))
                .metadata(RESPONSE_TIMEOUT_ATTR, 8000)
                .metadata(CONNECT_TIMEOUT_ATTR, 3000).uri("http://media-service:8084"))
                // Real-time Service Routes
                .route("realtime-service", r -> r.path("/api/realtime/**")
                .filters(f -> f
                .circuitBreaker(c -> c.setName("realtime-service").setFallbackUri("forward:/fallback/realtime")))
                .uri("http://realtime-service:8085"))
                // AI Service Routes
                .route("ai-service", r -> r.path("/api/ai/**")
                .filters(f -> f
                .circuitBreaker(c -> c.setName("ai-service").setFallbackUri("forward:/fallback/ai"))
                .requestRateLimiter(c -> c.setRateLimiter(searchServiceRateLimiter()).setKeyResolver(ipKeyResolver)))
                .metadata(RESPONSE_TIMEOUT_ATTR, 8000)
                .metadata(CONNECT_TIMEOUT_ATTR, 3000).uri("http://ai-service:8086"))
                // Search Service Routes
                .route("search-service", r -> r.path("/api/search/**")
                .filters(f -> f
                .circuitBreaker(c -> c.setName("search-service").setFallbackUri("forward:/fallback/search"))
                .requestRateLimiter(c -> c.setRateLimiter(searchServiceRateLimiter()).setKeyResolver(ipKeyResolver)))
                .metadata(RESPONSE_TIMEOUT_ATTR, 8000)
                .metadata(CONNECT_TIMEOUT_ATTR, 3000).uri("http://search-service:8087"))
                // Keycloak Routes
                .route("keycloak", r -> r.path("/realms/**", "/admin/**")
                .uri("http://keycloak:8080"))
                .build();
    }
}
