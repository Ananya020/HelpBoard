package com.helpboard.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.helpboard.backend.model.enums.RequestStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO for returning Request details.
 */
@Data
public class RequestResponse {
    private Long requestId;
    private Long itemId;
    private String itemTitle;
    private Long requesterId;
    private String requesterName;
    private Long ownerId; // Owner of the item
    private String ownerName; // Owner of the item
    private RequestStatus status;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime approvedAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime closedAt;
}