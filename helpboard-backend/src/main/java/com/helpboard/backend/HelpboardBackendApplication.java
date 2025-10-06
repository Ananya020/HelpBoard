package com.helpboard.backend;

import org.modelmapper.ModelMapper; // Import for ModelMapper
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class HelpboardBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(HelpboardBackendApplication.class, args);
    }

    /**
     * TODO: This PasswordEncoder is configured for plain text passwords as per requirements.
     *       Before any production use, replace this with BCryptPasswordEncoder or another
     *       strong hashing algorithm.
     *       Example: `return new BCryptPasswordEncoder();`
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    /**
     * Configures and provides a ModelMapper bean for DTO-to-Entity and Entity-to-DTO conversions.
     */
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        // Configure ModelMapper to be more flexible, e.g., to skip nulls on update
        modelMapper.getConfiguration().setSkipNullEnabled(true);
        return modelMapper;
    }
}