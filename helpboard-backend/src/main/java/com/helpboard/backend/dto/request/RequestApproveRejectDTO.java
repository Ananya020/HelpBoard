package com.helpboard.backend.dto.request;

import com.helpboard.backend.model.enums.RequestStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO for updating the status of a request (approve/reject/return).
 */
@Data
public class RequestApproveRejectDTO {
    @NotNull(message = "Request status is required")
    private RequestStatus status;
}