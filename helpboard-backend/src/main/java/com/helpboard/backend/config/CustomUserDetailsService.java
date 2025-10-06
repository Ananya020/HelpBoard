package com.helpboard.backend.config;

import com.helpboard.backend.model.User;
import com.helpboard.backend.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * Custom implementation of Spring Security's UserDetailsService to load user-specific data.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Locates the user based on the email (username).
     * In the actual system, the email is used as the unique identifier for login.
     *
     * @param email The email identifying the user whose data is required.
     * @return A {@link org.springframework.security.core.userdetails.UserDetails} object.
     * @throws UsernameNotFoundException if the user could not be found or has no granted authorities.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Note: As per requirements, passwords are stored in plain text.
        // The PasswordEncoder bean in HelpboardBackendApplication also returns NoOpPasswordEncoder.
        // For production, ensure both are updated to use BCrypt.
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), new ArrayList<>());
    }
}