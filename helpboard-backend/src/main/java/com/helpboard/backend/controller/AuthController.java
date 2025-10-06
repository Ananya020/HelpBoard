package com.helpboard.backend.controller;

import com.helpboard.backend.dto.auth.LoginRequest;
import com.helpboard.backend.dto.auth.LoginResponse;
import com.helpboard.backend.dto.auth.RegisterRequest;
import com.helpboard.backend.dto.user.UserResponse;
import com.helpboard.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for authentication related operations: user registration and login.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Handles user registration.
     *
     * @param request The registration request payload.
     * @return A {@link ResponseEntity} containing the registered {@link UserResponse} and HTTP status 201.
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody RegisterRequest request) {
        UserResponse newUser = authService.registerUser(request);
        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }

    /**
     * Handles user login and generates a JWT token.
     *
     * @param request The login request payload.
     * @return A {@link ResponseEntity} containing the {@link LoginResponse} with JWT token and user details, and HTTP status 200.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(@Valid @RequestBody LoginRequest request) {
        LoginResponse loginResponse = authService.loginUser(request);
        return ResponseEntity.ok(loginResponse);
    }
}