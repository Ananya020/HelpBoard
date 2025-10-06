package com.helpboard.backend.dto.item;

import com.helpboard.backend.model.enums.ItemType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO for creating a new Item.
 */
@Data
public class ItemCreateRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Category is required")
    @Size(max = 100, message = "Category cannot exceed 100 characters")
    private String category;

    @NotNull(message = "Item type is required")
    private ItemType type;

    @Size(max = 500, message = "Image URL cannot exceed 500 characters")
    private String imageUrl;
}