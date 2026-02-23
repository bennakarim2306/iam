package com.foodopia.backend.service;

import com.foodopia.backend.data.item.Item;
import com.foodopia.backend.data.item.ItemRepository;
import com.foodopia.backend.data.item.Seller;
import com.foodopia.backend.data.user.User;
import com.foodopia.backend.data.user.UserRepository;
import com.foodopia.backend.rest.v1.dto.ItemRequestDTO;
import com.foodopia.backend.rest.v1.dto.ItemResponseDTO;
import com.foodopia.backend.rest.v1.mapper.ItemMapper;
import com.foodopia.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemService {

    private static final Logger logger = LoggerFactory.getLogger(ItemService.class);
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public List<ItemResponseDTO> getAllItems() {
        logger.info("Fetching all items");
        List<Item> items = itemRepository.findAll();
        logger.info("Retrieved {} items", items.size());
        return items.stream()
                .map(itemMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public ItemResponseDTO addItemWithImage(String authHeader, ItemRequestDTO itemDTO, MultipartFile imageFile) {
        try {
            logger.info("Adding item with image: name={}", itemDTO.getName());

            // Konvertiere DTO zu Entity
            Item item = itemMapper.toEntity(itemDTO);

            // Generate UUID if not provided
            if (item.getId() == null || item.getId().isEmpty()) {
                item.setId(UUID.randomUUID().toString());
            }

            // Extrahiere E-Mail aus dem Authorization Bearer Token
            String userEmail = extractUserEmailFromAuthHeader(authHeader);
            User seller = findUserByEmail(userEmail);

            // Setze Seller-Informationen
            item.setSeller(Seller.builder()
                    .id(seller.getId())
                    .name(seller.getFirstName() + " " + seller.getLastName())
                    .contact(seller.getEmail())
                    .build());

            String base64Image = Base64.getEncoder().encodeToString(imageFile.getBytes());
            item.setImageUrl("data:" + imageFile.getContentType() + ";base64," + base64Image);

            Item saved = itemRepository.save(item);
            logger.info("Item added successfully: id={}, seller={}", saved.getId(), seller.getEmail());
            return itemMapper.toResponseDTO(saved);
        } catch (IOException e) {
            logger.error("errorCode=IMAGE_PROCESSING_ERROR errormessage=Failed to process image for item: name={}",
                    itemDTO.getName());
            if (logger.isDebugEnabled()) {
                logger.debug("stacktrace", e);
            }
            throw new RuntimeException("Bild konnte nicht verarbeitet werden", e);
        }
    }

    public ItemResponseDTO addItem(String authHeader, ItemRequestDTO itemDTO) {
        logger.info("Adding item without image: name={}", itemDTO.getName());

        // Konvertiere DTO zu Entity
        Item item = itemMapper.toEntity(itemDTO);

        // Generate UUID if not provided
        if (item.getId() == null || item.getId().isEmpty()) {
            item.setId(UUID.randomUUID().toString());
        }

        // Extrahiere E-Mail aus dem Authorization Bearer Token
        String userEmail = extractUserEmailFromAuthHeader(authHeader);
        User seller = findUserByEmail(userEmail);

        // Setze Seller-Informationen
        item.setSeller(Seller.builder()
                .id(seller.getId())
                .name(seller.getFirstName() + " " + seller.getLastName())
                .contact(seller.getEmail())
                .build());

        Item saved = itemRepository.save(item);
        logger.info("Item added successfully: id={}, seller={}", saved.getId(), seller.getEmail());
        return itemMapper.toResponseDTO(saved);
    }

    public ItemResponseDTO updateItemWithImage(String authHeader, String id, ItemRequestDTO itemDTO, MultipartFile imageFile) {
        logger.info("Updating item with image: id={}", id);

        Item item = itemRepository.findById(id).orElseThrow(() -> {
            logger.error("errorCode=ITEM_NOT_FOUND errormessage=Item not found: id={}", id);
            return new RuntimeException("Item nicht gefunden");
        });

        try {
            // Aktualisiere Item mit DTO-Daten
            itemMapper.updateEntityFromDTO(itemDTO, item);

            // Extrahiere E-Mail aus dem Authorization Bearer Token und aktualisiere Seller-Informationen
            String userEmail = extractUserEmailFromAuthHeader(authHeader);
            User seller = findUserByEmail(userEmail);
            item.setSeller(Seller.builder()
                    .id(seller.getId())
                    .name(seller.getFirstName() + " " + seller.getLastName())
                    .contact(seller.getEmail())
                    .build());

            // Aktualisiere Bild
            String base64Image = Base64.getEncoder().encodeToString(imageFile.getBytes());
            item.setImageUrl("data:" + imageFile.getContentType() + ";base64," + base64Image);

            Item saved = itemRepository.save(item);
            logger.info("Item updated successfully: id={}", id);
            return itemMapper.toResponseDTO(saved);
        } catch (IOException e) {
            logger.error("errorCode=IMAGE_PROCESSING_ERROR errormessage=Failed to process image for item update: id={}",
                    id);
            if (logger.isDebugEnabled()) {
                logger.debug("stacktrace", e);
            }
            throw new RuntimeException("Bild konnte nicht verarbeitet werden", e);
        }
    }

    public ItemResponseDTO updateItem(String authHeader, String id, ItemRequestDTO itemDTO) {
        logger.info("Updating item without image: id={}", id);

        Item item = itemRepository.findById(id).orElseThrow(() -> {
            logger.error("errorCode=ITEM_NOT_FOUND errormessage=Item not found: id={}", id);
            return new RuntimeException("Item nicht gefunden");
        });

        // Aktualisiere Item mit DTO-Daten
        itemMapper.updateEntityFromDTO(itemDTO, item);

        // Extrahiere E-Mail aus dem Authorization Bearer Token und aktualisiere Seller-Informationen
        String userEmail = extractUserEmailFromAuthHeader(authHeader);
        User seller = findUserByEmail(userEmail);
        item.setSeller(Seller.builder()
                .id(seller.getId())
                .name(seller.getFirstName() + " " + seller.getLastName())
                .contact(seller.getEmail())
                .build());

        Item saved = itemRepository.save(item);
        logger.info("Item updated successfully: id={}", id);
        return itemMapper.toResponseDTO(saved);
    }

    public void deleteItem(String id) {
        logger.info("Deleting item: id={}", id);
        itemRepository.deleteById(id);
        logger.info("Item deleted successfully: id={}", id);
    }

    public List<ItemResponseDTO> getFilteredItems(
            Optional<String> name,
            Optional<String> type,
            Optional<Double> minPrice,
            Optional<Double> maxPrice,
            Optional<Double> lat,
            Optional<Double> lng,
            Optional<Double> distanceKm
    ) {
        logger.info("Fetching filtered items: name={}, type={}, minPrice={}, maxPrice={}, lat={}, lng={}, distanceKm={}",
                name.orElse(null), type.orElse(null), minPrice.orElse(null),
                maxPrice.orElse(null), lat.orElse(null), lng.orElse(null), distanceKm.orElse(null));

        List<Item> items = itemRepository.findAll();
        List<Item> filtered = items.stream()
                .filter(item -> name.map(n -> item.getName().toLowerCase().contains(n.toLowerCase())).orElse(true))
                .filter(item -> type.map(t -> item.getType().equalsIgnoreCase(t)).orElse(true))
                .filter(item -> minPrice.map(min -> item.getPrice() >= min).orElse(true))
                .filter(item -> maxPrice.map(max -> item.getPrice() <= max).orElse(true))
                .filter(item -> {
                    if (lat.isPresent() && lng.isPresent() && distanceKm.isPresent()
                            && item.getAddress() != null) {
                        double d = distance(lat.get(), lng.get(), item.getAddress().getLat(), item.getAddress().getLng());
                        return d <= distanceKm.get();
                    }
                    return true;
                })
                .collect(Collectors.toList());

        logger.info("Retrieved {} filtered items out of {} total items", filtered.size(), items.size());
        return filtered.stream()
                .map(itemMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    private double distance(double lat1, double lng1, double lat2, double lng2) {
        final int R = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lng2 - lng1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    /**
     * Extrahiert die E-Mail-Adresse des Benutzers aus dem Authorization Bearer Token.
     * @param authHeader Der Authorization Header im Format "Bearer <token>"
     * @return Die E-Mail-Adresse des Benutzers
     */
    private String extractUserEmailFromAuthHeader(String authHeader) {
        String token = authHeader.substring(7); // Entferne "Bearer "
        return jwtService.extractUserEmail(token);
    }

    /**
     * Findet einen Benutzer anhand seiner E-Mail-Adresse.
     * @param email Die E-Mail-Adresse des Benutzers
     * @return Der gefundene Benutzer
     * @throws RuntimeException wenn der Benutzer nicht gefunden wird
     */
    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("errorCode=USER_NOT_FOUND errormessage=User not found with email: email={}", email);
                    return new RuntimeException("Benutzer nicht gefunden: " + email);
                });
    }
}
