package com.foodopia.backend.data.item;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "items")
@Table(name = "items", schema = "business_data")
public class Item {

    @Id
    private String id;

    private String name;
    private String type;
    private double price;
    private double quantity;
    private String unit;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "street", column = @Column(name = "address_street")),
        @AttributeOverride(name = "city", column = @Column(name = "address_city")),
        @AttributeOverride(name = "zip", column = @Column(name = "address_zip")),
        @AttributeOverride(name = "lat", column = @Column(name = "address_lat")),
        @AttributeOverride(name = "lng", column = @Column(name = "address_lng"))
    })
    private Address address;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "id", column = @Column(name = "seller_id")),
        @AttributeOverride(name = "name", column = @Column(name = "seller_name")),
        @AttributeOverride(name = "contact", column = @Column(name = "seller_contact"))
    })
    private Seller seller;

    @Column(name = "availablefrom")
    private java.time.LocalDate availableFrom;

    @Column(name = "availableto")
    private java.time.LocalDate availableTo;

    private String description;

    @Column(name = "imageurl")
    private String imageUrl;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    // Getters and Setters
}