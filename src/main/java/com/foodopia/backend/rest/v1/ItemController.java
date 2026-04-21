package com.foodopia.backend.rest.v1;

import com.foodopia.backend.rest.v1.dto.ItemRequestDTO;
import com.foodopia.backend.rest.v1.dto.ItemResponseDTO;
import com.foodopia.backend.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

/**
 * REST-Controller für die Verwaltung von Items (Angeboten).
 * Bietet Endpunkte zum Erstellen, Aktualisieren, Löschen und Abfragen von Items.
 * Unterstützt Bild-Upload (verschiedene Formate) und Filterung nach Parametern inkl. Geoposition.
 */
@RestController
@RequestMapping("api/v1/items")
@RequiredArgsConstructor
public class ItemController {

    // Service für die Geschäftslogik rund um Items
    private final ItemService itemService;

    /**
     * Gibt alle Items zurück.
     * GET /api/v1/items
     * @return Liste aller Items
     */
    @GetMapping
    public List<ItemResponseDTO> getAllItems() {
        return itemService.getAllItems();
    }

    /**
     * Legt ein neues Item mit Bild an.
     * POST /api/v1/items (multipart/form-data)
     * @param authHeader Der Authorization Bearer Token zur Extraktion von Seller-Informationen
     * @param itemJson Das Item-Objekt als JSON-String
     * @param imageFile Das Bild als MultipartFile
     * @return Das gespeicherte Item
     */
    @PostMapping(consumes = {"multipart/form-data"})
    public ItemResponseDTO addItemWithImage(
            @RequestHeader("Authorization") String authHeader,
            @RequestPart("item") ItemRequestDTO itemJson,
            @RequestPart("image") MultipartFile imageFile
    ) {
        return itemService.addItemWithImage(authHeader, itemJson, imageFile);
    }

    /**
     * Legt ein neues Item ohne Bild an.
     * POST /api/v1/items (application/json)
     * @param authHeader Der Authorization Bearer Token zur Extraktion von Seller-Informationen
     * @param item Das Item-Objekt
     * @return Das gespeicherte Item
     */
    @PostMapping(consumes = {"application/json"})
    public ItemResponseDTO addItem(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ItemRequestDTO item
    ) {
        return itemService.addItem(authHeader, item);
    }

    /**
     * Aktualisiert ein bestehendes Item inkl. Bild.
     * PUT /api/v1/items/{id} (multipart/form-data)
     * @param authHeader Der Authorization Bearer Token
     * @param id Die Item-ID
     * @param item Das aktualisierte Item-Objekt
     * @param imageFile Das neue Bild
     * @return Das aktualisierte Item
     */
    @PutMapping(path = "/{id}", consumes = {"multipart/form-data"})
    public ItemResponseDTO updateItemWithImage(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String id,
            @RequestPart("item") ItemRequestDTO item,
            @RequestPart("image") MultipartFile imageFile
    ) {
        return itemService.updateItemWithImage(authHeader, id, item, imageFile);
    }

    /**
     * Aktualisiert ein bestehendes Item ohne Bild.
     * PUT /api/v1/items/{id} (application/json)
     * @param authHeader Der Authorization Bearer Token
     * @param id Die Item-ID
     * @param item Das aktualisierte Item-Objekt
     * @return Das aktualisierte Item
     */
    @PutMapping(path = "/{id}", consumes = {"application/json"})
    public ItemResponseDTO updateItem(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String id,
            @RequestBody ItemRequestDTO item
    ) {
        return itemService.updateItem(authHeader, id, item);
    }

    /**
     * Löscht ein Item anhand der ID.
     * DELETE /api/v1/items/{id}
     * @param id Die Item-ID
     */
    @DeleteMapping("/{id}")
    public void deleteItem(@PathVariable String id) {
        itemService.deleteItem(id);
    }

    /**
     * Gibt alle Items des aktuellen Benutzers zurück.
     * Extrahiert die E-Mail-Adresse aus dem Authorization Bearer Token und
     * gibt alle Items zurück, bei denen seller.contact mit dieser E-Mail übereinstimmt.
     * GET /api/v1/items/user/my-items
     * @param authHeader Der Authorization Bearer Token
     * @return Liste der Items des aktuellen Benutzers
     */
    @GetMapping("/user/my-items")
    public List<ItemResponseDTO> getUserItems(
            @RequestHeader("Authorization") String authHeader
    ) {
        return itemService.getUserItems(authHeader);
    }

    /**
     * Gibt gefilterte Items zurück, z.B. nach Name, Typ, Preis, Geoposition und Distanz.
     * GET /api/v1/items/filter
     * @param name Optionaler Name-Filter
     * @param type Optionaler Typ-Filter
     * @param minPrice Optionaler Minimalpreis
     * @param maxPrice Optionaler Maximalpreis
     * @param lat Optionale Latitude für Geofilter
     * @param lng Optionale Longitude für Geofilter
     * @param distanceKm Optionale Distanz in km für Geofilter
     * @return Gefilterte Liste von Items
     */
    @GetMapping("/filter")
    public List<ItemResponseDTO> getFilteredItems(
            @RequestParam Optional<String> name,
            @RequestParam Optional<String> type,
            @RequestParam Optional<Double> minPrice,
            @RequestParam Optional<Double> maxPrice,
            @RequestParam Optional<Double> lat,
            @RequestParam Optional<Double> lng,
            @RequestParam Optional<Double> distanceKm
    ) {
        return itemService.getFilteredItems(name, type, minPrice, maxPrice, lat, lng, distanceKm);
    }
}