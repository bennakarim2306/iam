package com.foodopia.backend.security.auth;

import com.foodopia.backend.exception.AuthorizationResponseException;
import com.foodopia.backend.exception.UnregisteredUserException;
import com.foodopia.backend.exception.UserAlreadyExistingException;
import com.foodopia.backend.rest.v1.dto.AuthenticationRequest;
import com.foodopia.backend.rest.v1.dto.AuthenticationResponse;
import com.foodopia.backend.rest.v1.errorHandling.ErrorCode;
import com.foodopia.backend.security.JwtService;
import com.foodopia.backend.data.user.Role;
import com.foodopia.backend.data.user.User;
import com.foodopia.backend.data.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.foodopia.backend.rest.v1.errorHandling.ErrorCode.UNREGISTERED_USER;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegisterRequest request) throws UserAlreadyExistingException {
        var user = User.builder()
                .firstName(request.getFirstname())
                .lastName(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();
        if(userRepository.findByEmail(user.getEmail()).isPresent()) {
            String message = "User already existing with the email address provided" + user.getEmail();
            throw new UserAlreadyExistingException(message, ErrorCode.USER_ALREADY_REGISTERED);
        }

        userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) throws AuthorizationResponseException, AuthenticationException {
        var user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new UnregisteredUserException("User unregistered with the email address: " + request.getEmail(), UNREGISTERED_USER));
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthenticationResponse refreshToken(String refreshToken) throws AuthorizationResponseException {
        // Validate that this is actually a refresh token
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new AuthorizationResponseException("Invalid token type. Refresh token required.", ErrorCode.INVALID_TOKEN);
        }
        
        String userEmail = jwtService.extractUserEmail(refreshToken);
        
        if (userEmail == null) {
            throw new AuthorizationResponseException("Invalid refresh token", ErrorCode.INVALID_TOKEN);
        }
        
        var user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UnregisteredUserException(
                        "User not found with email: " + userEmail, 
                        UNREGISTERED_USER
                ));
        
        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new AuthorizationResponseException("Refresh token is invalid or expired", ErrorCode.INVALID_TOKEN);
        }
        
        var newJwtToken = jwtService.generateToken(user);
        var newRefreshToken = jwtService.generateRefreshToken(user);
        return AuthenticationResponse.builder()
                .token(newJwtToken)
                .refreshToken(newRefreshToken)
                .build();
    }
}
