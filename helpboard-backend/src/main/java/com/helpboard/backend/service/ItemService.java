package com.helpboard.backend.service;

import com.helpboard.backend.dto.item.ItemCreateRequest;
import com.helpboard.backend.dto.item.ItemResponse;
import com.helpboard.backend.dto.item.ItemUpdateRequest;
import com.helpboard.backend.exception.AccessDeniedException;
import com.helpboard.backend.exception.ResourceNotFoundException;
import com.helpboard.backend.model.Item;
import com.helpboard.backend.model.User;
import com.helpboard.backend.model.enums.ItemStatus;
import com.helpboard.backend.model.enums.ItemType;
import com.helpboard.backend.repository.ItemRepository;
import com.helpboard.backend.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing Item-related business logic.
 */
@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final AuthService authService; // To get current user ID
    private final ModelMapper modelMapper;

    public ItemService(ItemRepository itemRepository, UserRepository userRepository,
                       AuthService authService, ModelMapper modelMapper) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.authService = authService;
        this.modelMapper = modelMapper;
    }

    /**
     * Creates a new item.
     *
     * @param request The ItemCreateRequest DTO containing item details.
     * @return The created ItemResponse DTO.
     * @throws ResourceNotFoundException if the owner user is not found.
     */
    public ItemResponse createItem(ItemCreateRequest request) {
        Long currentUserId = authService.getCurrentUserId();
        User owner = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner user not found with ID: " + currentUserId));

        Item item = modelMapper.map(request, Item.class);
        item.setOwner(owner);
        item.setStatus(ItemStatus.AVAILABLE); // New items are available by default
        item.setCreatedAt(LocalDateTime.now());

        Item savedItem = itemRepository.save(item);
        return mapItemToResponse(savedItem);
    }

    /**
     * Retrieves an item by its ID.
     *
     * @param itemId The ID of the item to retrieve.
     * @return The ItemResponse DTO.
     * @throws ResourceNotFoundException if the item is not found.
     */
    public ItemResponse getItemById(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with ID: " + itemId));
        return mapItemToResponse(item);
    }

    /**
     * Retrieves all items, with optional filtering.
     *
     * @param type     Optional ItemType to filter by.
     * @param category Optional category to filter by.
     * @param location Optional owner's location to filter by.
     * @param ownerId  Optional owner's ID to filter by.
     * @param status   Optional ItemStatus to filter by.
     * @return A list of ItemResponse DTOs.
     */
    public List<ItemResponse> getAllItems(ItemType type, String category, String location, Long ownerId, ItemStatus status) {
        List<Item> items = itemRepository.findByCriteria(type, category, location, ownerId, status);
        return items.stream()
                .map(this::mapItemToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Updates an existing item.
     *
     * @param itemId  The ID of the item to update.
     * @param request The ItemUpdateRequest DTO with updated fields.
     * @return The updated ItemResponse DTO.
     * @throws ResourceNotFoundException if the item is not found.
     * @throws AccessDeniedException     if the current user is not the owner of the item.
     */
    public ItemResponse updateItem(Long itemId, ItemUpdateRequest request) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with ID: " + itemId));

        Long currentUserId = authService.getCurrentUserId();
        if (!item.getOwner().getUserId().equals(currentUserId)) {
            throw new AccessDeniedException("You are not authorized to update this item.");
        }

        modelMapper.map(request, item); // Maps non-null fields from request to item

        Item updatedItem = itemRepository.save(item);
        return mapItemToResponse(updatedItem);
    }

    /**
     * Deletes an item by its ID.
     *
     * @param itemId The ID of the item to delete.
     * @throws ResourceNotFoundException if the item is not found.
     * @throws AccessDeniedException     if the current user is not the owner of the item.
     */
    public void deleteItem(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with ID: " + itemId));

        Long currentUserId = authService.getCurrentUserId();
        if (!item.getOwner().getUserId().equals(currentUserId)) {
            throw new AccessDeniedException("You are not authorized to delete this item.");
        }

        itemRepository.delete(item);
    }

    /**
     * Helper method to map an Item entity to an ItemResponse DTO.
     *
     * @param item The Item entity.
     * @return The ItemResponse DTO.
     */
    private ItemResponse mapItemToResponse(Item item) {
        ItemResponse response = modelMapper.map(item, ItemResponse.class);
        response.setOwnerId(item.getOwner().getUserId());
        response.setOwnerName(item.getOwner().getName());
        return response;
    }
}