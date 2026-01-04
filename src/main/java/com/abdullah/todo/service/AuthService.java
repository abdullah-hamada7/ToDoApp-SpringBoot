package com.abdullah.todo.service;

import com.abdullah.todo.dto.AuthResponse;
import com.abdullah.todo.dto.LoginRequest;
import com.abdullah.todo.dto.RefreshRequest;
import com.abdullah.todo.dto.RegisterRequest;
import com.abdullah.todo.entity.User;
import com.abdullah.todo.repository.UserRepository;
import com.abdullah.todo.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Authentication Service.
 * Handles user registration, login, and token refresh.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    /**
     * Register a new user.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getUsername());

        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + request.getUsername());
        }

        // Create new user with USER role
        User user = new User(
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()),
                Set.of("ROLE_USER"));
        userRepository.save(user);

        log.info("User registered successfully: {}", request.getUsername());

        // Generate tokens
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        return generateAuthResponse(userDetails);
    }

    /**
     * Authenticate user and return tokens.
     */
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()));
        } catch (BadCredentialsException e) {
            log.warn("Failed login attempt for user: {}", request.getUsername());
            throw new BadCredentialsException("Invalid username or password");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        log.info("User logged in successfully: {}", request.getUsername());

        return generateAuthResponse(userDetails);
    }

    /**
     * Refresh access token using refresh token.
     */
    public AuthResponse refresh(RefreshRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtService.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        String username = jwtService.extractUsername(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (!jwtService.isTokenValid(refreshToken, userDetails)) {
            throw new IllegalArgumentException("Refresh token is invalid or expired");
        }

        log.info("Token refreshed for user: {}", username);

        // Generate new access token only (keep using same refresh token until it
        // expires)
        String accessToken = jwtService.generateAccessToken(userDetails);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(accessTokenExpiration / 1000) // Convert to seconds
                .username(username)
                .build();
    }

    /**
     * Generate auth response with tokens.
     */
    private AuthResponse generateAuthResponse(UserDetails userDetails) {
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(accessTokenExpiration / 1000) // Convert to seconds
                .username(userDetails.getUsername())
                .build();
    }
}
