package com.foodopia.backend.rest.v1.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenRequest {
    
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}