package com.foodopia.backend.rest.v1;

import com.foodopia.backend.exception.AuthorizationResponseException;
import com.foodopia.backend.rest.v1.dto.AuthenticationRequest;
import com.foodopia.backend.rest.v1.dto.AuthenticationResponse;
import com.foodopia.backend.rest.v1.dto.RefreshTokenRequest;
import com.foodopia.backend.security.auth.AuthenticationService;
import com.foodopia.backend.security.auth.RegisterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest request
    ) throws AuthorizationResponseException {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody @Valid AuthenticationRequest request
    ) throws AuthorizationResponseException {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refresh(
            @RequestBody @Valid RefreshTokenRequest request
    ) throws AuthorizationResponseException {
        return ResponseEntity.ok(authenticationService.refreshToken(request.getRefreshToken()));
    }
}
