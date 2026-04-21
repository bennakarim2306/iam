package com.foodopia.backend.rest.v1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lightweight DTO für Item-Informationen in Transaktionen.
 * Enthält nur die wichtigsten Metadaten und ein Thumbnail (128x128).
 *
 * Wird verwendet um Performance zu optimieren, indem große Vollbilder
 * nicht bei jeder Transaction-Abfrage übertragen werden.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemSummaryDTO {

    /**
     * Item Name
     */
    private String name;

    /**
     * Item Type (z.B. "Vegetables", "Fruits")
     */
    private String type;

    /**
     * Preis pro Einheit
     */
    private double price;

    /**
     * Einheit (z.B. "kg", "pieces")
     */
    private String unit;

    /**
     * Thumbnail-Bild (128x128) im Base64 Format
     * Format: "data:image/jpeg;base64,..."
     *
     * Kann null sein für alte Items ohne Thumbnail
     */
    private String thumbnailUrl;
}

