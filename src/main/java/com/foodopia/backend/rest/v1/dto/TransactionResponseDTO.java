package com.foodopia.backend.rest.v1.dto;

import com.foodopia.backend.data.transaction.TransactionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO für ausgehende Transaction-Responses
 * Wird für GET und alle Responses zurückgegeben
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponseDTO {

    /**
     * Eindeutige Transaktions-ID
     */
    @Schema(description = "Eindeutige Transaktions-ID", example = "trans-001")
    private String id;

    /**
     * Die ID des bestellten Angebots (Item)
     */
    @Schema(description = "ID des Angebots", example = "item-123")
    private String itemId;

    /**
     * Zusammenfassung des Items (mit Thumbnail)
     */
    @Schema(description = "Item-Informationen mit Thumbnail")
    private ItemSummaryDTO item;

    /**
     * E-Mail des Kunden
     */
    @Schema(description = "E-Mail des Kunden", example = "customer@example.com")
    private String customerEmail;

    /**
     * E-Mail des Verkäufers
     */
    @Schema(description = "E-Mail des Verkäufers", example = "seller@example.com")
    private String sellerEmail;

    /**
     * Die bestellte Menge
     */
    @Schema(description = "Bestellmenge", example = "2.5")
    private double quantityOrdered;

    /**
     * Der aktuelle Transaktionsstatus
     */
    @Schema(description = "Transaktionsstatus", example = "PENDING", allowableValues = {"PENDING", "CONFIRMED", "COMPLETED", "CANCELLED", "REJECTED"})
    private TransactionStatus status;

    /**
     * Preis pro Einheit zum Zeitpunkt der Bestellung (Snapshot)
     */
    @Schema(description = "Preis pro Einheit", example = "4.99")
    private double pricePerUnit;

    /**
     * Gesamtpreis der Bestellung
     */
    @Schema(description = "Gesamtpreis (Menge × Preis pro Einheit)", example = "12.475")
    private double totalPrice;

    /**
     * Optionale Notizen zur Transaktion
     */
    @Schema(description = "Notizen zur Bestellung", example = "Bitte frisch verpacken")
    private String notes;

    /**
     * Zeitstempel der Erstellung
     */
    @Schema(description = "Zeitstempel der Erstellung", example = "2025-09-24T10:30:00")
    private LocalDateTime createdAt;

    /**
     * Zeitstempel der letzten Aktualisierung
     */
    @Schema(description = "Zeitstempel der letzten Aktualisierung", example = "2025-09-24T10:35:00")
    private LocalDateTime updatedAt;
}
