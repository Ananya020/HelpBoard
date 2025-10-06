package com.helpboard.backend.controller;

import com.helpboard.backend.dto.request.RequestResponse;
import com.helpboard.backend.dto.user.UserResponse;
import com.helpboard.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing user-related data.
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param userId The ID of the user.
     * @return A {@link ResponseEntity} containing the {@link UserResponse}.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
        UserResponse user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    /**
     * Retrieves all requests associated with a user, either as a requester or an item owner.
     *
     * @param userId The ID of the user.
     * @param role   Optional query parameter to filter requests by role ("requester" or "owner").
     * @return A {@link ResponseEntity} containing a list of {@link RequestResponse} DTOs.
     */
    @GetMapping("/{userId}/requests")
    public ResponseEntity<List<RequestResponse>> getUserRequests(
            @PathVariable Long userId,
            @RequestParam(required = false) String role) {
        List<RequestResponse> requests = userService.getUserRequests(userId, role);
        return ResponseEntity.ok(requests);
    }
}