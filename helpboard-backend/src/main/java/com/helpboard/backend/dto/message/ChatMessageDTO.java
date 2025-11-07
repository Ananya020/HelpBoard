package com.helpboard.backend.dto.message;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for sending/receiving chat messages via WebSocket.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    // For incoming messages from client (only messageText is sent by client)
    @NotBlank(message = "Message text cannot be empty")
    private String messageText;

    // These fields are populated by the server before broadcasting/saving
    private Long messageId;
    private Long requestId;
    private Long senderId;
    private String senderName; // For display on client side
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
}