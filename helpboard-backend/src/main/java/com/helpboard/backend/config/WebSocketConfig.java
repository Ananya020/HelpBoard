package com.helpboard.backend.config;

import com.helpboard.backend.util.JwtUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.Objects;

/**
 * Configures WebSocket and STOMP message broker.
 * Enables STOMP over WebSocket at `/ws` and sets up authentication for WebSocket connections.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public WebSocketConfig(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Registers STOMP endpoints.
     *
     * @param registry The StompEndpointRegistry.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                // Limit allowed origins to the Next.js dev server for local development
                .setAllowedOrigins("http://localhost:3000")
                .withSockJS(); // Enable SockJS fallback options
    }

    /**
     * Configures the message broker.
     *
     * @param registry The MessageBrokerRegistry.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Prefix for messages sent from the server to the client (topics)
        registry.enableSimpleBroker("/topic");
        // Prefix for messages sent from the client to the server (app destinations)
        registry.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Registers interceptors for the client inbound channel to handle WebSocket authentication.
     *
     * @param registration The ChannelRegistration.
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                // Authenticate CONNECT and SUBSCRIBE commands
                if (accessor != null && (StompCommand.CONNECT.equals(accessor.getCommand()) || StompCommand.SUBSCRIBE.equals(accessor.getCommand()))) {
                    String authToken = accessor.getFirstNativeHeader("Authorization"); // For CONNECT
                    if (authToken == null) {
                        // Fallback: Some STOMP clients might put it in the session attributes or as query param
                        // This example assumes it's in the Authorization header.
                        // For SockJS, the header needs to be passed in the SockJS constructor's 'headers' option.
                        // e.g. new SockJS('/ws', null, { headers: { 'Authorization': 'Bearer <token>' } });
                    }

                    if (authToken != null && authToken.startsWith("Bearer ")) {
                        String jwt = authToken.substring(7);
                        try {
                            String username = jwtUtil.extractUsername(jwt);
                            if (username != null) {
                                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                                if (jwtUtil.validateToken(jwt, userDetails)) {
                                    UsernamePasswordAuthenticationToken authentication =
                                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                                    accessor.setUser(authentication); // Set the authenticated user
                                    return message;
                                }
                            }
                        } catch (Exception e) {
                            // Log and reject connection on invalid token
                            System.err.println("WebSocket JWT authentication failed: " + e.getMessage());
                            throw new RuntimeException("Invalid JWT token for WebSocket connection", e);
                        }
                    }
                    // If no valid token, connection or subscription will be unauthorized
                    if (accessor.getUser() == null) {
                        System.err.println("Unauthorized WebSocket connection attempt: No valid JWT token.");
                        throw new RuntimeException("Unauthorized: No valid JWT token provided.");
                    }
                }
                return message;
            }
        });
    }
}