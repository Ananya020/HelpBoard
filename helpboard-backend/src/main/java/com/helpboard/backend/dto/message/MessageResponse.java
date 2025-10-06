package com.helpboard.backend.dto.message;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO for returning message history via REST API.
 */
@Data
public class MessageResponse {
    private Long messageId;
    private Long requestId;
    private Long senderId;
    private String senderName;
    private String messageText;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
}