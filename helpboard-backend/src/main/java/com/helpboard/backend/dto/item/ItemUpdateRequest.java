package com.helpboard.backend.dto.item;

import com.helpboard.backend.model.enums.ItemStatus;
import com.helpboard.backend.model.enums.ItemType;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO for updating an existing Item. All fields are optional, only provided ones will be updated.
 */
@Data
public class ItemUpdateRequest {
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;

    private String description;

    @Size(max = 100, message = "Category cannot exceed 100 characters")
    private String category;

    private ItemType type;

    @Size(max = 500, message = "Image URL cannot exceed 500 characters")
    private String imageUrl;

    private ItemStatus status;
}