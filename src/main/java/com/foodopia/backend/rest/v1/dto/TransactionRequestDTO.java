package com.foodopia.backend.rest.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;

/**
 * DTO für eingehende Transaction-Requests
 * Wird für POST (neue Transaktion) und PUT (aktualisierte Transaktion) verwendet
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionRequestDTO {

    /**
     * Die ID des Angebots (Item), das bestellt wird
     * Erforderlich für neue Transaktionen
     */
    @NotBlank(message = "Item ID is required")
    @Schema(description = "ID des Angebots (Item)", example = "item-123")
    private String itemId;

    /**
     * Die bestellte Menge
     * Muss größer als 0 sein
     */
    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.01", message = "Quantity must be greater than 0")
    @Schema(description = "Bestellmenge", example = "2.5")
    private Double quantityOrdered;

    /**
     * Optionale Notizen/Kommentare zur Bestellung
     * z.B. Lieferpräferenzen, spezielle Anforderungen
     */
    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    @Schema(description = "Optionale Notizen zur Bestellung", example = "Bitte frisch verpacken")
    private String notes;
}
