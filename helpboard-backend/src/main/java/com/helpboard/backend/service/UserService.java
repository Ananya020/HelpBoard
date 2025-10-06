package com.helpboard.backend.service;

import com.helpboard.backend.dto.request.RequestResponse;
import com.helpboard.backend.dto.user.UserResponse;
import com.helpboard.backend.exception.ResourceNotFoundException;
import com.helpboard.backend.model.User;
import com.helpboard.backend.repository.RequestRepository;
import com.helpboard.backend.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for user-related operations.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final ModelMapper modelMapper;

    public UserService(UserRepository userRepository, RequestRepository requestRepository, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
        this.modelMapper = modelMapper;
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param userId The ID of the user to retrieve.
     * @return The UserResponse DTO.
     * @throws ResourceNotFoundException if the user is not found.
     */
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        return modelMapper.map(user, UserResponse.class);
    }

    /**
     * Retrieves requests associated with a user, either as a requester or an item owner.
     *
     * @param userId The ID of the user.
     * @param role   Optional role filter ("requester" or "owner").
     * @return A list of RequestResponse DTOs.
     * @throws ResourceNotFoundException if the user is not found.
     */
    public List<RequestResponse> getUserRequests(Long userId, String role) {
        // Ensure user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }

        List<com.helpboard.backend.model.Request> requests;
        if (role == null) {
            requests = requestRepository.findByRequesterOrOwnerId(userId);
        } else if ("requester".equalsIgnoreCase(role)) {
            requests = requestRepository.findByRequesterUserId(userId);
        } else if ("owner".equalsIgnoreCase(role)) {
            requests = requestRepository.findByItemOwnerUserId(userId);
        } else {
            throw new IllegalArgumentException("Invalid role parameter. Must be 'requester', 'owner', or null.");
        }

        return requests.stream()
                .map(request -> {
                    RequestResponse dto = modelMapper.map(request, RequestResponse.class);
                    // Populate additional fields from related entities
                    dto.setItemId(request.getItem().getItemId());
                    dto.setItemTitle(request.getItem().getTitle());
                    dto.setRequesterId(request.getRequester().getUserId());
                    dto.setRequesterName(request.getRequester().getName());
                    dto.setOwnerId(request.getItem().getOwner().getUserId());
                    dto.setOwnerName(request.getItem().getOwner().getName());
                    return dto;
                })
                .collect(Collectors.toList());
    }
}