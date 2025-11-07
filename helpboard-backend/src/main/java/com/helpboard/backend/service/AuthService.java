package com.helpboard.backend.service;

import com.helpboard.backend.dto.auth.LoginRequest;
import com.helpboard.backend.dto.auth.LoginResponse;
import com.helpboard.backend.dto.auth.RegisterRequest;
import com.helpboard.backend.dto.user.UserResponse;
import com.helpboard.backend.exception.AccessDeniedException;
import com.helpboard.backend.exception.ResourceNotFoundException;
import com.helpboard.backend.model.User;
import com.helpboard.backend.repository.UserRepository;
import com.helpboard.backend.util.JwtUtil;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * Service for user authentication and registration.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final ModelMapper modelMapper;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil, AuthenticationManager authenticationManager, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.modelMapper = modelMapper;
    }

    /**
     * Registers a new user.
     *
     * @param request The registration request DTO.
     * @return The created UserResponse DTO.
     * @throws AccessDeniedException if a user with the given email already exists.
     */
    public UserResponse registerUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AccessDeniedException("Email already registered.");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        // TODO: replace plain text password storage with BCryptPasswordEncoder before any production use
        //       Ensure the PasswordEncoder bean in HelpboardBackendApplication is also updated.
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Encoder will be NoOpPasswordEncoder
        user.setLocation(request.getLocation());
        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, UserResponse.class);
    }

    /**
     * Logs in a user and generates a JWT token.
     *
     * @param request The login request DTO.
     * @return The LoginResponse DTO with JWT token and user details.
     * @throws BadCredentialsException if authentication fails.
     */
    public LoginResponse loginUser(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found after authentication"));

            String token = jwtUtil.generateToken(user.getEmail(), user.getUserId());
            Date expirationDate = jwtUtil.extractExpiration(token);
            LocalDateTime expiresAt = jwtUtil.toLocalDateTime(expirationDate);

            UserResponse userResponse = modelMapper.map(user, UserResponse.class);

            return new LoginResponse("Bearer " + token, expiresAt, userResponse);

        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid email or password.");
        } catch (UsernameNotFoundException e) {
            throw new ResourceNotFoundException("User not found with email: " + request.getEmail());
        }
    }

    /**
     * Retrieves the currently authenticated user's ID.
     *
     * @return The userId of the authenticated user.
     * @throws IllegalStateException if no user is authenticated.
     */
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
            // Assuming principal is UserDetails, and its username is the email
            String email = ((org.springframework.security.core.userdetails.User) authentication.getPrincipal()).getUsername();
            return userRepository.findByEmail(email)
                    .map(User::getUserId)
                    .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database."));
        }
        throw new IllegalStateException("User not authenticated.");
    }

    /**
     * Retrieves a user's ID by email.
     *
     * @param email The email of the user.
     * @return The userId of the user.
     * @throws IllegalStateException if user is not found.
     */
    public Long getUserIdByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(User::getUserId)
                .orElseThrow(() -> new IllegalStateException("User not found with email: " + email));
    }
}