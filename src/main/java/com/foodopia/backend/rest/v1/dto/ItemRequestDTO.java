package com.foodopia.backend.rest.v1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO für eingehende Item-Requests.
 * Verwendet flache Struktur für Adressdaten, wie sie vom Client gesendet werden.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemRequestDTO {
    private String id;
    private String name;
    private String type;
    private Double price;
    private Double quantity;
    private String unit;

    // Flache Adressfelder
    private String street;
    private String city;
    private String zip;
    private Double lat;
    private Double lng;

    // Seller-Felder
    private String sellerId;
    private String sellerName;
    private String sellerContact;

    private String availableFrom; // Format: "yyyy-MM-dd"
    private String availableTo;   // Format: "yyyy-MM-dd"
    private String description;
    // imageUrl wird nicht hier definiert, da es vom Service berechnet wird
}
