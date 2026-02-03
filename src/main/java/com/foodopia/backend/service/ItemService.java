package com.foodopia.backend.service;

import com.foodopia.backend.data.item.Item;
import com.foodopia.backend.data.item.ItemRepository;
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

    public List<Item> getAllItems() {
        logger.info("Fetching all items");
        List<Item> items = itemRepository.findAll();
        logger.info("Retrieved {} items", items.size());
        return items;
    }

    public Item addItemWithImage(Item item, MultipartFile imageFile) {
        String uuid = UUID.randomUUID().toString();
        try {
            logger.info("uuid={} Adding item with image: name={}", uuid, item.getName());
            String base64Image = Base64.getEncoder().encodeToString(imageFile.getBytes());
            item.setImageUrl("data:" + imageFile.getContentType() + ";base64," + base64Image);
            Item saved = itemRepository.save(item);
            logger.info("uuid={} Item added successfully: id={}", uuid, saved.getId());
            return saved;
        } catch (IOException e) {
            logger.error("uuid={} errorCode=IMAGE_PROCESSING_ERROR errormessage=Failed to process image for item: name={}",
                    uuid, item.getName());
            if (logger.isDebugEnabled()) {
                logger.debug("uuid={} stacktrace", uuid, e);
            }
            throw new RuntimeException("Bild konnte nicht verarbeitet werden", e);
        }
    }

    public Item addItem(Item item) {
        String uuid = UUID.randomUUID().toString();
        logger.info("uuid={} Adding item without image: name={}", uuid, item.getName());
        Item saved = itemRepository.save(item);
        logger.info("uuid={} Item added successfully: id={}", uuid, saved.getId());
        return saved;
    }

    public Item updateItemWithImage(String id, Item updatedItem, MultipartFile imageFile) {
        String uuid = UUID.randomUUID().toString();
        logger.info("uuid={} Updating item with image: id={}", uuid, id);

        Item item = itemRepository.findById(id).orElseThrow(() -> {
            logger.error("uuid={} errorCode=ITEM_NOT_FOUND errormessage=Item not found: id={}", uuid, id);
            return new RuntimeException("Item nicht gefunden");
        });

        try {
            String base64Image = Base64.getEncoder().encodeToString(imageFile.getBytes());
            updatedItem.setImageUrl("data:" + imageFile.getContentType() + ";base64," + base64Image);
            updatedItem.setId(id);
            Item saved = itemRepository.save(updatedItem);
            logger.info("uuid={} Item updated successfully: id={}", uuid, id);
            return saved;
        } catch (IOException e) {
            logger.error("uuid={} errorCode=IMAGE_PROCESSING_ERROR errormessage=Failed to process image for item update: id={}",
                    uuid, id);
            if (logger.isDebugEnabled()) {
                logger.debug("uuid={} stacktrace", uuid, e);
            }
            throw new RuntimeException("Bild konnte nicht verarbeitet werden", e);
        }
    }

    public Item updateItem(String id, Item updatedItem) {
        String uuid = UUID.randomUUID().toString();
        logger.info("uuid={} Updating item without image: id={}", uuid, id);

        if (!itemRepository.existsById(id)) {
            logger.error("uuid={} errorCode=ITEM_NOT_FOUND errormessage=Item not found: id={}", uuid, id);
            throw new RuntimeException("Item nicht gefunden");
        }

        updatedItem.setId(id);
        Item saved = itemRepository.save(updatedItem);
        logger.info("uuid={} Item updated successfully: id={}", uuid, id);
        return saved;
    }

    public void deleteItem(String id) {
        String uuid = UUID.randomUUID().toString();
        logger.info("uuid={} Deleting item: id={}", uuid, id);
        itemRepository.deleteById(id);
        logger.info("uuid={} Item deleted successfully: id={}", uuid, id);
    }

    public List<Item> getFilteredItems(
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
        return filtered;
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
}
