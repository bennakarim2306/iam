package com.foodopia.backend.rest.v1.mapper;

import com.foodopia.backend.data.item.Address;
import com.foodopia.backend.data.item.Item;
import com.foodopia.backend.data.item.Seller;
import com.foodopia.backend.rest.v1.dto.ItemRequestDTO;
import com.foodopia.backend.rest.v1.dto.ItemResponseDTO;
import com.foodopia.backend.rest.v1.dto.ItemSummaryDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Mapper für die Konvertierung zwischen Item-Entity und DTOs.
 */
@Component
public class ItemMapper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * Konvertiert ein ItemRequestDTO in eine Item-Entity.
     */
    public Item toEntity(ItemRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Address address = null;
        if (dto.getStreet() != null || dto.getCity() != null || dto.getZip() != null) {
            address = Address.builder()
                    .street(dto.getStreet())
                    .city(dto.getCity())
                    .zip(dto.getZip())
                    .lat(dto.getLat() != null ? dto.getLat() : 0.0)
                    .lng(dto.getLng() != null ? dto.getLng() : 0.0)
                    .build();
        }

        Seller seller = null;
        if (dto.getSellerId() != null || dto.getSellerName() != null || dto.getSellerContact() != null) {
            seller = Seller.builder()
                    .id(dto.getSellerId())
                    .name(dto.getSellerName())
                    .contact(dto.getSellerContact())
                    .build();
        }

        return Item.builder()
                .id(dto.getId())
                .name(dto.getName())
                .type(dto.getType())
                .price(dto.getPrice() != null ? dto.getPrice() : 0.0)
                .quantity(dto.getQuantity() != null ? dto.getQuantity() : 0.0)
                .unit(dto.getUnit())
                .address(address)
                .seller(seller)
                .availableFrom(parseDate(dto.getAvailableFrom()))
                .availableTo(parseDate(dto.getAvailableTo()))
                .description(dto.getDescription())
                .build();
    }

    /**
     * Konvertiert eine Item-Entity in ein ItemResponseDTO.
     */
    public ItemResponseDTO toResponseDTO(Item item) {
        if (item == null) {
            return null;
        }

        ItemResponseDTO.AddressDTO addressDTO = null;
        if (item.getAddress() != null) {
            addressDTO = ItemResponseDTO.AddressDTO.builder()
                    .street(item.getAddress().getStreet())
                    .city(item.getAddress().getCity())
                    .zip(item.getAddress().getZip())
                    .lat(item.getAddress().getLat())
                    .lng(item.getAddress().getLng())
                    .build();
        }

        ItemResponseDTO.SellerDTO sellerDTO = null;
        if (item.getSeller() != null) {
            sellerDTO = ItemResponseDTO.SellerDTO.builder()
                    .id(item.getSeller().getId())
                    .name(item.getSeller().getName())
                    .contact(item.getSeller().getContact())
                    .build();
        }

        return ItemResponseDTO.builder()
                .id(item.getId())
                .name(item.getName())
                .type(item.getType())
                .price(item.getPrice())
                .quantity(item.getQuantity())
                .unit(item.getUnit())
                .address(addressDTO)
                .seller(sellerDTO)
                .availableFrom(formatDate(item.getAvailableFrom()))
                .availableTo(formatDate(item.getAvailableTo()))
                .description(item.getDescription())
                .imageUrl(item.getImageUrl())
                .build();
    }

    /**
     * Aktualisiert eine bestehende Item-Entity mit Daten aus einem ItemRequestDTO.
     * Die ID und imageUrl werden nicht überschrieben.
     */
    public void updateEntityFromDTO(ItemRequestDTO dto, Item item) {
        if (dto == null || item == null) {
            return;
        }

        if (dto.getName() != null) {
            item.setName(dto.getName());
        }
        if (dto.getType() != null) {
            item.setType(dto.getType());
        }
        if (dto.getPrice() != null) {
            item.setPrice(dto.getPrice());
        }
        if (dto.getQuantity() != null) {
            item.setQuantity(dto.getQuantity());
        }
        if (dto.getUnit() != null) {
            item.setUnit(dto.getUnit());
        }

        // Address aktualisieren
        if (dto.getStreet() != null || dto.getCity() != null || dto.getZip() != null) {
            Address address = item.getAddress() != null ? item.getAddress() : new Address();
            if (dto.getStreet() != null) address.setStreet(dto.getStreet());
            if (dto.getCity() != null) address.setCity(dto.getCity());
            if (dto.getZip() != null) address.setZip(dto.getZip());
            if (dto.getLat() != null) address.setLat(dto.getLat());
            if (dto.getLng() != null) address.setLng(dto.getLng());
            item.setAddress(address);
        }

        // Seller aktualisieren
        if (dto.getSellerId() != null || dto.getSellerName() != null || dto.getSellerContact() != null) {
            Seller seller = item.getSeller() != null ? item.getSeller() : new Seller();
            if (dto.getSellerId() != null) seller.setId(dto.getSellerId());
            if (dto.getSellerName() != null) seller.setName(dto.getSellerName());
            if (dto.getSellerContact() != null) seller.setContact(dto.getSellerContact());
            item.setSeller(seller);
        }

        if (dto.getAvailableFrom() != null) {
            item.setAvailableFrom(parseDate(dto.getAvailableFrom()));
        }
        if (dto.getAvailableTo() != null) {
            item.setAvailableTo(parseDate(dto.getAvailableTo()));
        }
        if (dto.getDescription() != null) {
            item.setDescription(dto.getDescription());
        }
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }

    private String formatDate(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.format(DATE_FORMATTER);
    }

    /**
     * Konvertiert eine Item-Entity in ein ItemSummaryDTO (für Transaktionen).
     * Enthält nur die wichtigsten Metadaten und Thumbnail.
     */
    public ItemSummaryDTO toSummaryDTO(Item item) {
        if (item == null) {
            return null;
        }

        return ItemSummaryDTO.builder()
                .name(item.getName())
                .type(item.getType())
                .price(item.getPrice())
                .unit(item.getUnit())
                .thumbnailUrl(item.getThumbnailUrl())
                .build();
    }
}
