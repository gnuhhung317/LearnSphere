package com.studyhub.chat_service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

/**
 * Intercepts WebSocket messages to extract and validate JWT from STOMP headers
 */
@Slf4j
@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtDecoder jwtDecoder;

    public WebSocketAuthInterceptor(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null) {
            // Handle CONNECT - extract and validate JWT
            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                String authHeader = accessor.getFirstNativeHeader("Authorization");

                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);

                    try {
                        // Decode and validate JWT
                        Jwt jwt = jwtDecoder.decode(token);

                        // Create authentication
                        UsernamePasswordAuthenticationToken authentication
                                = new UsernamePasswordAuthenticationToken(jwt, null, null);

                        // Store in accessor user for this WebSocket session
                        accessor.setUser(authentication);

                        // Store JWT in session attributes for subsequent messages
                        accessor.getSessionAttributes().put("jwt", jwt);
                        accessor.getSessionAttributes().put("authentication", authentication);

                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        log.info("WebSocket authenticated with JWT for user: {}", jwt.getSubject());

                    } catch (JwtException e) {
                        log.error("Invalid JWT token in WebSocket connection: {}", e.getMessage());
                        throw new IllegalArgumentException("Invalid JWT token");
                    }
                } else {
                    log.warn("No Authorization header found in WebSocket CONNECT");
                }
            } // For all other messages (SEND, SUBSCRIBE), restore authentication from session
            else {
                log.info("Processing {} message - session attributes: {}", 
                    accessor.getCommand(), 
                    accessor.getSessionAttributes() != null ? "present" : "null");
                
                if (accessor.getSessionAttributes() != null) {
                    UsernamePasswordAuthenticationToken authentication
                            = (UsernamePasswordAuthenticationToken) accessor.getSessionAttributes().get("authentication");

                    if (authentication != null) {
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.info("✅ Restored authentication for WebSocket message: {}", accessor.getCommand());
                    } else {
                        log.error("❌ Authentication not found in session for message: {}", accessor.getCommand());
                    }
                } else {
                    log.error("❌ Session attributes are null for message: {}", accessor.getCommand());
                }
            }
        }

        return message;
    }
}
