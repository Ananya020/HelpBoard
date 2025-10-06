package com.helpboard.backend.controller;

import com.helpboard.backend.dto.item.ItemCreateRequest;
import com.helpboard.backend.dto.item.ItemResponse;
import com.helpboard.backend.dto.item.ItemUpdateRequest;
import com.helpboard.backend.model.enums.ItemStatus;
import com.helpboard.backend.model.enums.ItemType;
import com.helpboard.backend.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing Item resources.
 */
@RestController
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    /**
     * Creates a new item. Protected endpoint.
     *
     * @param request The item creation request payload.
     * @return A {@link ResponseEntity} containing the created {@link ItemResponse} and HTTP status 201.
     */
    @PostMapping
    public ResponseEntity<ItemResponse> createItem(@Valid @RequestBody ItemCreateRequest request) {
        ItemResponse newItem = itemService.createItem(request);
        return new ResponseEntity<>(newItem, HttpStatus.CREATED);
    }

    /**
     * Retrieves a list of items. Public endpoint, with optional filters.
     *
     * @param type     Optional query parameter for item type (BORROW, LEND, DONATE).
     * @param category Optional query parameter for item category.
     * @param location Optional query parameter for owner's location.
     * @param ownerId  Optional query parameter for owner's user ID.
     * @param status   Optional query parameter for item status (AVAILABLE, REQUESTED, APPROVED, COMPLETED).
     * @return A {@link ResponseEntity} containing a list of {@link ItemResponse} DTOs.
     */
    @GetMapping
    public ResponseEntity<List<ItemResponse>> getAllItems(
            @RequestParam(required = false) ItemType type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) ItemStatus status) {
        List<ItemResponse> items = itemService.getAllItems(type, category, location, ownerId, status);
        return ResponseEntity.ok(items);
    }

    /**
     * Retrieves a single item by its ID. Public endpoint.
     *
     * @param itemId The ID of the item.
     * @return A {@link ResponseEntity} containing the {@link ItemResponse}.
     */
    @GetMapping("/{itemId}")
    public ResponseEntity<ItemResponse> getItemById(@PathVariable Long itemId) {
        ItemResponse item = itemService.getItemById(itemId);
        return ResponseEntity.ok(item);
    }

    /**
     * Updates an existing item. Protected endpoint, only owner can update.
     *
     * @param itemId  The ID of the item to update.
     * @param request The item update request payload.
     * @return A {@link ResponseEntity} containing the updated {@link ItemResponse}.
     */
    @PutMapping("/{itemId}")
    public ResponseEntity<ItemResponse> updateItem(@PathVariable Long itemId,
                                                 @Valid @RequestBody ItemUpdateRequest request) {
        ItemResponse updatedItem = itemService.updateItem(itemId, request);
        return ResponseEntity.ok(updatedItem);
    }

    /**
     * Deletes an item. Protected endpoint, only owner can delete.
     *
     * @param itemId The ID of the item to delete.
     * @return A {@link ResponseEntity} with no content and HTTP status 204.
     */
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long itemId) {
        itemService.deleteItem(itemId);
        return ResponseEntity.noContent().build();
    }
}