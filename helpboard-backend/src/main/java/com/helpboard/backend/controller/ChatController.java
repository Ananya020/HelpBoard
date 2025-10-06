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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
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
    public void sendChatMessage(@DestinationVariable Long requestId, @Valid @Payload ChatMessageDTO chatMessageDTO) {
        Long senderId = authService.getCurrentUserId(); // Authenticated user from WebSocket session
        Message savedMessage = messageService.saveMessage(requestId, senderId, chatMessageDTO.getMessageText());

        // Prepare DTO for broadcasting (includes senderName and timestamp)
        ChatMessageDTO broadcastDTO = messageService.mapMessageToChatMessageDTO(savedMessage);

        // Broadcast the message to all subscribers of this request's topic
        messagingTemplate.convertAndSend("/topic/requests/" + requestId, broadcastDTO);
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