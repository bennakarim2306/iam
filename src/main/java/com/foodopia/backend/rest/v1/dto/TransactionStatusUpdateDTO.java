package com.foodopia.backend.rest.v1.dto;

import com.foodopia.backend.data.transaction.TransactionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

/**
 * DTO für Status-Update Requests
 * Wird für PUT /api/v1/transactions/{id}/status verwendet
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionStatusUpdateDTO {

    /**
     * Der neue Status für die Transaktion
     */
    @NotNull(message = "Status is required")
    @Schema(description = "Neuer Transaktionsstatus", example = "CONFIRMED", allowableValues = {"CONFIRMED", "COMPLETED", "CANCELLED", "REJECTED"})
    private TransactionStatus status;
}
