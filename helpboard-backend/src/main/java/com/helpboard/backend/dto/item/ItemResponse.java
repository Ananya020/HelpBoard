package com.helpboard.backend.dto.item;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.helpboard.backend.model.enums.ItemStatus;
import com.helpboard.backend.model.enums.ItemType;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO for returning Item details.
 */
@Data
public class ItemResponse {
    private Long itemId;
    private Long ownerId;
    private String ownerName;
    private String title;
    private String description;
    private String category;
    private ItemType type;
    private String imageUrl;
    private ItemStatus status;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}