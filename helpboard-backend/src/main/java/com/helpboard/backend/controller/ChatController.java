package com.helpboard.backend.controller;

import com.helpboard.backend.dto.message.ChatMessageDTO;
import com.helpboard.backend.dto.message.MessageResponse;
import com.helpboard.backend.model.Message;
import com.helpboard.backend.service.AuthService;
import com.helpboard.backend.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Handles both REST API for chat history and WebSocket messages for real-time chat.
 */
@Controller
public class ChatController {

    private final MessageService messageService;
    private final AuthService authService;
    private final SimpMessagingTemplate messagingTemplate; // Used to send messages to WebSocket clients

    public ChatController(MessageService messageService, AuthService authService, SimpMessagingTemplate messagingTemplate) {
        this.messageService = messageService;
        this.authService = authService;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * WebSocket endpoint for sending chat messages.
     * Messages sent to `/app/requests/{requestId}/send` will be processed and broadcast.
     *
     * @param requestId The ID of the request to which the message belongs.
     * @param chatMessageDTO The message payload from the client.
     */
    @MessageMapping("/requests/{requestId}/send")
    public void sendChatMessage(
            @DestinationVariable Long requestId, 
            @Valid @Payload ChatMessageDTO chatMessageDTO,
            SimpMessageHeaderAccessor headerAccessor) {
        try {
            System.out.println("Received message for request " + requestId + ": " + chatMessageDTO.getMessageText());
            
            // Get authenticated user from message header (set during CONNECT)
            Long senderId = null;
            if (headerAccessor != null && headerAccessor.getUser() != null) {
                Object principal = headerAccessor.getUser();
                if (principal instanceof UsernamePasswordAuthenticationToken) {
                    UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) principal;
                    UserDetails userDetails = (UserDetails) auth.getPrincipal();
                    senderId = authService.getUserIdByEmail(userDetails.getUsername());
                }
            }
            
            if (senderId == null) {
                System.err.println("Error: Could not determine sender ID from WebSocket session");
                throw new IllegalStateException("User not authenticated in WebSocket session");
            }
            
            System.out.println("Sender ID: " + senderId);
            
            Message savedMessage = messageService.saveMessage(requestId, senderId, chatMessageDTO.getMessageText());
            System.out.println("Message saved with ID: " + savedMessage.getMessageId());

            // Prepare DTO for broadcasting (includes senderName and timestamp)
            ChatMessageDTO broadcastDTO = messageService.mapMessageToChatMessageDTO(savedMessage);
            System.out.println("Broadcasting message to /topic/requests/" + requestId);

            // Broadcast the message to all subscribers of this request's topic
            messagingTemplate.convertAndSend("/topic/requests/" + requestId, broadcastDTO);
            System.out.println("Message broadcast successful");
        } catch (Exception e) {
            System.err.println("Error processing chat message: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw to let STOMP handle the error
        }
    }

    /**
     * REST endpoint to retrieve chat history for a given request. Protected endpoint.
     *
     * @param requestId The ID of the request.
     * @return A {@link ResponseEntity} containing a list of {@link MessageResponse} DTOs.
     */
    @GetMapping("/requests/{requestId}/messages")
    public ResponseEntity<List<MessageResponse>> getChatHistory(@PathVariable Long requestId) {
        List<MessageResponse> messages = messageService.getChatHistory(requestId);
        return ResponseEntity.ok(messages);
    }

    // Health Check endpoint
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("{\"status\": \"UP\"}");
    }
}