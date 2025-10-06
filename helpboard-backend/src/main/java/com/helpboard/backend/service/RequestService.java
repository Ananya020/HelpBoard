package com.helpboard.backend.service;

import com.helpboard.backend.dto.request.RequestApproveRejectDTO;
import com.helpboard.backend.dto.request.RequestResponse;
import com.helpboard.backend.exception.AccessDeniedException;
import com.helpboard.backend.exception.ResourceNotFoundException;
import com.helpboard.backend.model.Item;
import com.helpboard.backend.model.Request;
import com.helpboard.backend.model.User;
import com.helpboard.backend.model.enums.ItemStatus;
import com.helpboard.backend.model.enums.RequestStatus;
import com.helpboard.backend.repository.ItemRepository;
import com.helpboard.backend.repository.RequestRepository;
import com.helpboard.backend.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing Request-related business logic.
 */
@Service
public class RequestService {

    private final RequestRepository requestRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final ModelMapper modelMapper;
    private final SimpMessagingTemplate messagingTemplate; // For WebSocket communication

    public RequestService(RequestRepository requestRepository, ItemRepository itemRepository,
                          UserRepository userRepository, AuthService authService,
                          ModelMapper modelMapper, SimpMessagingTemplate messagingTemplate) {
        this.requestRepository = requestRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.authService = authService;
        this.modelMapper = modelMapper;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Creates a new request for an item.
     *
     * @param itemId The ID of the item to request.
     * @return The created RequestResponse DTO.
     * @throws ResourceNotFoundException if the item or requester is not found.
     * @throws AccessDeniedException     if the requester is the item owner or an active request already exists.
     */
    @Transactional
    public RequestResponse createRequest(Long itemId) {
        Long currentUserId = authService.getCurrentUserId();
        User requester = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Requester user not found with ID: " + currentUserId));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with ID: " + itemId));

        if (item.getOwner().getUserId().equals(currentUserId)) {
            throw new AccessDeniedException("You cannot request your own item.");
        }
        if (item.getStatus() != ItemStatus.AVAILABLE) {
            throw new AccessDeniedException("Item is not available for request.");
        }
        if (requestRepository.findByItemItemIdAndStatusIn(itemId, Arrays.asList(RequestStatus.PENDING, RequestStatus.APPROVED)).isPresent()) {
            throw new AccessDeniedException("An active request for this item already exists.");
        }

        Request request = new Request();
        request.setItem(item);
        request.setRequester(requester);
        request.setStatus(RequestStatus.PENDING);
        request.setTimestamp(LocalDateTime.now());

        item.setStatus(ItemStatus.REQUESTED); // Mark item as requested
        itemRepository.save(item);

        Request savedRequest = requestRepository.save(request);
        return mapRequestToResponse(savedRequest);
    }

    /**
     * Updates the status of a request (approve, reject, return).
     *
     * @param requestId The ID of the request to update.
     * @param dto       The RequestApproveRejectDTO containing the new status.
     * @return The updated RequestResponse DTO.
     * @throws ResourceNotFoundException if the request is not found.
     * @throws AccessDeniedException     if the current user is not the item owner, or invalid status transition.
     */
    @Transactional
    public RequestResponse updateRequestStatus(Long requestId, RequestApproveRejectDTO dto) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with ID: " + requestId));

        Long currentUserId = authService.getCurrentUserId();
        if (!request.getItem().getOwner().getUserId().equals(currentUserId)) {
            throw new AccessDeniedException("You are not authorized to update the status of this request.");
        }

        RequestStatus newStatus = dto.getStatus();
        RequestStatus currentStatus = request.getStatus();
        Item item = request.getItem();

        // Validate status transitions
        switch (newStatus) {
            case APPROVED:
                if (currentStatus != RequestStatus.PENDING) {
                    throw new AccessDeniedException("Only PENDING requests can be APPROVED.");
                }
                request.setStatus(RequestStatus.APPROVED);
                request.setApprovedAt(LocalDateTime.now());
                item.setStatus(ItemStatus.APPROVED); // Item becomes approved
                break;
            case REJECTED:
                if (currentStatus != RequestStatus.PENDING) {
                    throw new AccessDeniedException("Only PENDING requests can be REJECTED.");
                }
                request.setStatus(RequestStatus.REJECTED);
                request.setClosedAt(LocalDateTime.now());
                item.setStatus(ItemStatus.AVAILABLE); // Item becomes available again
                break;
            case RETURNED:
                if (currentStatus != RequestStatus.APPROVED) {
                    throw new AccessDeniedException("Only APPROVED requests can be marked as RETURNED.");
                }
                request.setStatus(RequestStatus.RETURNED);
                request.setClosedAt(LocalDateTime.now());
                item.setStatus(ItemStatus.COMPLETED); // Item is completed
                break;
            default:
                throw new IllegalArgumentException("Invalid status for update: " + newStatus);
        }

        itemRepository.save(item);
        Request updatedRequest = requestRepository.save(request);

        // If approved, notify clients interested in this request for chat
        if (newStatus == RequestStatus.APPROVED) {
            // This is a placeholder for a more complex notification system.
            // For chat, simply enabling the topic is enough.
            // We could send a 'request approved' message to the participants if needed.
            System.out.println("Request " + requestId + " approved. Chat topic /topic/requests/" + requestId + " is now active.");
            // Optionally, send a system message to the chat channel
            // messagingTemplate.convertAndSend("/topic/requests/" + requestId,
            //         new ChatMessageDTO("System", "Request has been APPROVED! Chat is now open.", requestId, null, LocalDateTime.now()));
        }

        return mapRequestToResponse(updatedRequest);
    }

    /**
     * Retrieves a single request by its ID.
     *
     * @param requestId The ID of the request.
     * @return The RequestResponse DTO.
     * @throws ResourceNotFoundException if the request is not found.
     * @throws AccessDeniedException     if the current user is not a participant (requester or owner) of the request.
     */
    public RequestResponse getRequestById(Long requestId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with ID: " + requestId));

        Long currentUserId = authService.getCurrentUserId();
        if (!request.getRequester().getUserId().equals(currentUserId) &&
            !request.getItem().getOwner().getUserId().equals(currentUserId)) {
            throw new AccessDeniedException("You are not a participant of this request.");
        }
        return mapRequestToResponse(request);
    }

    /**
     * Helper method to map a Request entity to a RequestResponse DTO.
     *
     * @param request The Request entity.
     * @return The RequestResponse DTO.
     */
    private RequestResponse mapRequestToResponse(Request request) {
        RequestResponse response = modelMapper.map(request, RequestResponse.class);
        response.setItemId(request.getItem().getItemId());
        response.setItemTitle(request.getItem().getTitle());
        response.setRequesterId(request.getRequester().getUserId());
        response.setRequesterName(request.getRequester().getName());
        response.setOwnerId(request.getItem().getOwner().getUserId());
        response.setOwnerName(request.getItem().getOwner().getName());
        return response;
    }
}