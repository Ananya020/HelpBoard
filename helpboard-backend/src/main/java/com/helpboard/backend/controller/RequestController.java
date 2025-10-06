package com.helpboard.backend.controller;

import com.helpboard.backend.dto.request.RequestApproveRejectDTO;
import com.helpboard.backend.dto.request.RequestResponse;
import com.helpboard.backend.service.RequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for managing Request resources.
 */
@RestController
public class RequestController {

    private final RequestService requestService;

    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    /**
     * Creates a new request for a specific item. Protected endpoint.
     *
     * @param itemId The ID of the item for which to create a request.
     * @return A {@link ResponseEntity} containing the created {@link RequestResponse} and HTTP status 201.
     */
    @PostMapping("/items/{itemId}/request")
    public ResponseEntity<RequestResponse> createRequest(@PathVariable Long itemId) {
        RequestResponse newRequest = requestService.createRequest(itemId);
        return new ResponseEntity<>(newRequest, HttpStatus.CREATED);
    }

    /**
     * Updates the status of a request (e.g., APPROVED, REJECTED, RETURNED). Protected endpoint.
     * Only the item owner can perform this action.
     *
     * @param requestId The ID of the request to update.
     * @param dto       The {@link RequestApproveRejectDTO} specifying the new status.
     * @return A {@link ResponseEntity} containing the updated {@link RequestResponse}.
     */
    @PatchMapping("/requests/{requestId}/status")
    public ResponseEntity<RequestResponse> updateRequestStatus(
            @PathVariable Long requestId,
            @Valid @RequestBody RequestApproveRejectDTO dto) {
        RequestResponse updatedRequest = requestService.updateRequestStatus(requestId, dto);
        return ResponseEntity.ok(updatedRequest);
    }

    /**
     * Retrieves a single request by its ID. Protected endpoint, only participants can view.
     *
     * @param requestId The ID of the request.
     * @return A {@link ResponseEntity} containing the {@link RequestResponse}.
     */
    @GetMapping("/requests/{requestId}")
    public ResponseEntity<RequestResponse> getRequestById(@PathVariable Long requestId) {
        RequestResponse request = requestService.getRequestById(requestId);
        return ResponseEntity.ok(request);
    }
}