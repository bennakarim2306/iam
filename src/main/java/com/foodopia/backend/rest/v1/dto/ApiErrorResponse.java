// src/main/java/com/foodopia/backend/rest/v1/dto/ApiErrorResponse.java
package com.foodopia.backend.rest.v1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ApiErrorResponse {
    private final String errormessage;
    private final String errorCode;
    private final String errorUUID;
}
