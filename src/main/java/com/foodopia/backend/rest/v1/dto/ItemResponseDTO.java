package com.foodopia.backend.rest.v1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO für ausgehende Item-Responses.
 * Verwendet verschachtelte Struktur für bessere API-Klarheit.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemResponseDTO {
    private String id;
    private String name;
    private String type;
    private Double price;
    private Double quantity;
    private String unit;

    private AddressDTO address;
    private SellerDTO seller;

    private String availableFrom;
    private String availableTo;
    private String description;
    private String imageUrl;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AddressDTO {
        private String street;
        private String city;
        private String zip;
        private Double lat;
        private Double lng;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SellerDTO {
        private String id;
        private String name;
        private String contact;
    }
}
