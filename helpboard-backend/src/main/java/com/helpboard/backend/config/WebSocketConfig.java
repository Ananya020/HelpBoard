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
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

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
        .setAllowedOriginPatterns("http://localhost:3000", "http://127.0.0.1:3000")
        .withSockJS();
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
                
                if (accessor != null) {
                    StompCommand command = accessor.getCommand();
                    
                    // Authenticate CONNECT command
                    if (StompCommand.CONNECT.equals(command)) {
                        String authToken = accessor.getFirstNativeHeader("Authorization");
                        
                        if (authToken != null && authToken.startsWith("Bearer ")) {
                            String jwt = authToken.substring(7);
                            try {
                                String username = jwtUtil.extractUsername(jwt);
                                if (username != null) {
                                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                                    if (jwtUtil.validateToken(jwt, userDetails)) {
                                        UsernamePasswordAuthenticationToken authentication =
                                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                                        accessor.setUser(authentication); // Set the authenticated user - this persists for the session
                                        SecurityContextHolder.getContext().setAuthentication(authentication);
                                        
                                        // Also store in session attributes as backup
                                        SimpMessageHeaderAccessor simpAccessor = MessageHeaderAccessor.getAccessor(message, SimpMessageHeaderAccessor.class);
                                        if (simpAccessor != null) {
                                            java.util.Map<String, Object> sessionAttrs = simpAccessor.getSessionAttributes();
                                            if (sessionAttrs != null) {
                                                sessionAttrs.put("SPRING_SECURITY_CONTEXT", authentication);
                                                System.out.println("WebSocket interceptor: Stored authentication in session attributes during CONNECT");
                                            } else {
                                                System.out.println("WebSocket interceptor: Session attributes not available during CONNECT");
                                            }
                                        }
                                        
                                        System.out.println("WebSocket interceptor: CONNECT authenticated for user: " + username);
                                        return message;
                                    }
                                }
                            } catch (Exception e) {
                                // Log and reject connection on invalid token
                                System.err.println("WebSocket JWT authentication failed: " + e.getMessage());
                                throw new RuntimeException("Invalid JWT token for WebSocket connection", e);
                            }
                        }
                        // If no valid token, connection will be unauthorized
                        if (accessor.getUser() == null) {
                            System.err.println("Unauthorized WebSocket connection attempt: No valid JWT token.");
                            throw new RuntimeException("Unauthorized: No valid JWT token provided.");
                        }
                    }
                    // For SEND, SUBSCRIBE, and other commands, restore authentication from session
                    else if (command != null && command != StompCommand.DISCONNECT) {
                        System.out.println("WebSocket interceptor: Processing " + command + " command");
                        
                        // Try to get authentication from accessor first
                        if (accessor.getUser() != null && accessor.getUser() instanceof UsernamePasswordAuthenticationToken) {
                            UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) accessor.getUser();
                            SecurityContextHolder.getContext().setAuthentication(auth);
                            System.out.println("WebSocket interceptor: Authentication restored from accessor for " + command);
                        } else {
                            // Fallback: try to get from session attributes
                            SimpMessageHeaderAccessor simpAccessor = MessageHeaderAccessor.getAccessor(message, SimpMessageHeaderAccessor.class);
                            if (simpAccessor != null && simpAccessor.getSessionAttributes() != null) {
                                Object sessionAuth = simpAccessor.getSessionAttributes().get("SPRING_SECURITY_CONTEXT");
                                if (sessionAuth instanceof UsernamePasswordAuthenticationToken) {
                                    UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) sessionAuth;
                                    accessor.setUser(auth);
                                    SecurityContextHolder.getContext().setAuthentication(auth);
                                    System.out.println("WebSocket interceptor: Authentication restored from session attributes for " + command);
                                } else {
                                    System.err.println("WebSocket interceptor: No authentication found in session attributes for " + command);
                                }
                            } else {
                                System.err.println("WebSocket interceptor: No session attributes available for " + command);
                            }
                            
                            // If still no authentication, log warning
                            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                                System.err.println("WebSocket interceptor: WARNING - No authentication set for " + command + " command. User: " + accessor.getUser());
                            }
                        }
                    }
                }
                
                return message;
            }
        });
    }
}